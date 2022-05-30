package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListException
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListException.ErrorCode
import de.rki.coronawarnapp.dccticketing.core.allowlist.filtering.DccTicketingJwkFilter
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.DccTicketingAllowListRepository
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.MIN_VERSION_REQUIRED
import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.encode
import timber.log.Timber
import javax.inject.Inject

class DccTicketingQrCodeHandler @Inject constructor(
    private val requestService: DccTicketingRequestService,
    private val dccTicketingJwkFilter: DccTicketingJwkFilter,
    private val allowListRepository: DccTicketingAllowListRepository,
    private val qrCodeSettings: DccTicketingQrCodeSettings,
    private val appConfigProvider: AppConfigProvider,
) {
    suspend fun handleQrCode(qrCode: DccTicketingQrCode): DccTicketingTransactionContext {
        checkValidationServiceMinVersion()

        val container = allowListRepository.refresh()

        container.serviceProviderAllowList.validateServiceIdentity(
            qrCode.data.serviceIdentity
        )

        val validationServiceAllowList = container.validationServiceAllowList

        val transactionContext = DccTicketingTransactionContext(
            initializationData = qrCode.data
        ).decorate(validationServiceAllowList)

        val filteringResult = dccTicketingJwkFilter.filter(
            transactionContext.validationServiceJwkSet.orEmpty(),
            validationServiceAllowList
        )
        if (filteringResult.filteredJwkSet.isEmpty()) {
            throw DccTicketingAllowListException(ErrorCode.ALLOWLIST_NO_MATCH)
        }

        return transactionContext.copy(
            allowlist = filteringResult.filteredAllowlist,
            validationServiceJwkSet = filteringResult.filteredJwkSet
        )
    }

    @Throws(DccTicketingException::class)
    private suspend fun checkValidationServiceMinVersion() {
        val validationServiceMinVersion = appConfigProvider.currentConfig.first().validationServiceMinVersion
        when {
            validationServiceMinVersion > BuildConfigWrap.VERSION_CODE -> {
                Timber.tag(TAG).w(
                    "Validation service min version check failed minConfigV=%s,appV=%s",
                    validationServiceMinVersion,
                    BuildConfigWrap.VERSION_CODE
                )
                throw DccTicketingException(MIN_VERSION_REQUIRED)
            }
            else -> Timber.tag(TAG).w(
                "Validation service min version check passed minConfigV=%s,appV=%s",
                validationServiceMinVersion,
                BuildConfigWrap.VERSION_CODE
            )
        }
    }

    private suspend fun Set<DccTicketingServiceProviderAllowListEntry>.validateServiceIdentity(serviceIdentity: String) {
        if (!qrCodeSettings.checkServiceIdentity.first()) {
            Timber.i("Service identity check is turned off.")
            return
        }
        Timber.tag(TAG).v("Service identity check is turned on.")
        Timber.tag(TAG).v("Allowed hashes are $this.")

        val hash = serviceIdentity.toHash().also {
            Timber.tag(TAG).v("Calculated hash of service identity is $it")
        }
        find { it.serviceIdentityHash == hash }
            ?: throw DccTicketingAllowListException(ErrorCode.SP_ALLOWLIST_NO_MATCH).also {
                Timber.tag(TAG).e("Service identity check failed.")
            }

        Timber.tag(TAG).i("Service identity check passed.")
    }

    suspend fun DccTicketingTransactionContext.decorate(
        validationServiceAllowList: Set<DccTicketingValidationServiceAllowListEntry>
    ): DccTicketingTransactionContext {
        val decorator =
            requestService.requestValidationDecorator(
                initializationData.serviceIdentity,
                validationServiceAllowList
            )
        return copy(
            accessTokenService = decorator.accessTokenService,
            accessTokenServiceJwkSet = decorator.accessTokenServiceJwkSet,
            accessTokenSignJwkSet = decorator.accessTokenSignJwkSet,
            validationService = decorator.validationService,
            validationServiceJwkSet = decorator.validationServiceJwkSet,
        )
    }

    companion object {
        private val TAG = tag<DccTicketingQrCodeHandler>()
    }
}

internal fun String.toHash() = encode().sha256()
