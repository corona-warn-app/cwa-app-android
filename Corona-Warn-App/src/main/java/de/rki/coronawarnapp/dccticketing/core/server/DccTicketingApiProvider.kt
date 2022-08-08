package de.rki.coronawarnapp.dccticketing.core.server

import dagger.Lazy
import de.rki.coronawarnapp.dccticketing.core.DccTicketingHttpClient
import de.rki.coronawarnapp.dccticketing.core.common.DccJWKConverter
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class DccTicketingApiProvider @Inject constructor(
    @DccTicketingHttpClient private val defaultClient: OkHttpClient,
    private val gsonConverterFactory: GsonConverterFactory,
    private val dccJWKConverter: DccJWKConverter,
    private val dccTicketingApiV1Lazy: Lazy<DccTicketingApiV1>,
) {
    private val _dccTicketingApiV1: DccTicketingApiV1
        get() = dccTicketingApiV1Lazy.get()

    fun getDccTicketingApiV1() = _dccTicketingApiV1

    fun getDccTicketingApiV1(
        url: String,
        jwkSet: Set<DccJWK>
    ): DccTicketingApiV1 {
        val certificatePinner = dccJWKConverter.getCertificatePinner(url, jwkSet)
        val client = defaultClient.newBuilder()
            .apply {
                certificatePinner(certificatePinner)
            }.build()
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .baseUrl(url)
            .build()
            .create(DccTicketingApiV1::class.java)
    }

//    // Takes the first 8 bytes of the SHA-256 fingerprint of the certificate and encodes them with base64
//    private fun Certificate.createKid(): String = createSha256Fingerprint()
//        .substring(0, BYTE_COUNT)
//        .base64()
//
//    private fun Set<DccJWK>.findRequiredJwkSet(requiredKid: String): Set<DccJWK> {
//        val requiredJwkSet = filter { it.kid == requiredKid }.toSet()
//
//        if (requiredJwkSet.isEmpty()) {
//            throw DccTicketingServerCertificateCheckException(DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID)
//        }
//
//        return requiredJwkSet
//    }
}

private const val BYTE_COUNT: Int = 8
