package de.rki.coronawarnapp.dccticketing.core.transaction

import com.google.gson.Gson
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingDecoratorApi
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_CLIENT_ERR
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_NO_ATS
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_NO_ATS_SIGN_KEY
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_NO_ATS_SVC_KEY
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_NO_NETWORK
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_NO_VS
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_NO_VS_SVC_KEY
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_PARSE_ERR
import de.rki.coronawarnapp.dccticketing.core.decorator.DccTicketingServiceException.ErrorCode.VD_ID_SERVER_ERR
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class DccTicketingServiceDecorator @Inject constructor(
    private val api: DccTicketingDecoratorApi,
    @BaseGson private val gson: Gson,
    private val networkStateProvider: NetworkStateProvider,
) {

    /*
    * Throws DccTicketingServiceException
    * */
    suspend fun decorate(serviceEndpoint: String): Decoration {
        if (!networkStateProvider.networkState.first().isInternetAvailable) {
            throw DccTicketingServiceException(VD_ID_NO_NETWORK)
        }

        val response = api.getIdentityDocument(serviceEndpoint)
        if (!response.isSuccessful) {
            when (response.code()) {
                in 400..499 -> throw DccTicketingServiceException(VD_ID_CLIENT_ERR)
                in 500..599 -> throw DccTicketingServiceException(VD_ID_SERVER_ERR)
            }
        }
        val document = api.getIdentityDocument(serviceEndpoint).body()?.string().parse()

        return Decoration(
            DccTicketingAccessTokenService(
                accessTokenService = document.findAccessTokenService(),
                accessTokenServiceJwkSet = document.findAccessTokenServiceJwkSet(),
                accessTokenSignJwkSet = document.findAccessTokenSignJwkSet()
            ),

            DccTicketingValidationService(
                validationService = document.findValidationService(),
                validationServiceJwkSet = document.findValidationServiceJwkSet()
            )
        )
    }

    private fun String?.parse(): DccTicketingServiceIdentityDocument {
        return try {
            gson.fromJson(this!!)
        } catch (e: Exception) {
            throw DccTicketingServiceException(VD_ID_PARSE_ERR)
        }
    }

    private fun DccTicketingServiceIdentityDocument.findAccessTokenService(): DccTicketingService {
        return service?.find {
            it.type == "AccessTokenService"
        }?.also {
            Timber.v("AccessTokenService found: $it")
        } ?: throw DccTicketingServiceException(VD_ID_NO_ATS)
    }

    private fun DccTicketingServiceIdentityDocument.findAccessTokenSignJwkSet(): Set<DccJWK> {
        val regex = """/AccessTokenSignKey-\d+\$/""".toRegex()
        return verificationMethod.filter {
            regex.matches(it.id)
        }.mapNotNull {
            it.publicKeyJwk
        }.also {
            if (it.isEmpty()) throw DccTicketingServiceException(VD_ID_NO_ATS_SIGN_KEY)
            Timber.v("AccessTokenSignJwkSet found: $it")
        }.toSet()
    }

    private fun DccTicketingServiceIdentityDocument.findAccessTokenServiceJwkSet(): Set<DccJWK> {
        val regex = """/AccessTokenServiceKey-\d+\$/""".toRegex()
        return verificationMethod.filter {
            regex.matches(it.id)
        }.mapNotNull {
            it.publicKeyJwk
        }.also {
            if (it.isEmpty()) throw DccTicketingServiceException(VD_ID_NO_ATS_SVC_KEY)
            Timber.v("AccessTokenServiceJwkSet found: $it")
        }.toSet()
    }

    private fun DccTicketingServiceIdentityDocument.findValidationService(): DccTicketingService {
        return service?.find {
            it.type == "ValidationService"
        }?.also {
            Timber.v("ValidationService found: $it")
        } ?: throw DccTicketingServiceException(VD_ID_NO_VS)
    }

    private fun DccTicketingServiceIdentityDocument.findValidationServiceJwkSet(): Set<DccJWK> {
        val regex = """/ValidationServiceKey-\d+\$/""".toRegex()
        return verificationMethod.filter {
            regex.matches(it.id)
        }.mapNotNull {
            it.publicKeyJwk
        }.also {
            if (it.isEmpty()) throw DccTicketingServiceException(VD_ID_NO_VS_SVC_KEY)
            Timber.v("ValidationServiceJwkSet found: $it")
        }.toSet()
    }

    data class Decoration(
        val tokenService: DccTicketingAccessTokenService,
        val validationService: DccTicketingValidationService
    )
}

