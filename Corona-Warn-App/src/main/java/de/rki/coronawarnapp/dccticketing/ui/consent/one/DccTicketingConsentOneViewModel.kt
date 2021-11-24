package de.rki.coronawarnapp.dccticketing.ui.consent.one

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryption.ec.EcKeyGenerator
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DccTicketingConsentOneViewModel @AssistedInject constructor(
    @Assisted private val dccTicketingSharedViewModel: DccTicketingSharedViewModel,
    dispatcherProvider: DispatcherProvider,
    private val dccTicketingRequestService: DccTicketingRequestService,
    private val ecKeyGenerator: EcKeyGenerator
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val currentIsLoading = MutableStateFlow(false)
    val isLoading: LiveData<Boolean> = currentIsLoading.asLiveData()

    private val currentEvent = SingleLiveEvent<DccTicketingConsentOneEvent>()
    val events = currentEvent

    private val currentUiState = dccTicketingSharedViewModel.transactionContext
        .map { UiState(it) }
    val uiState: LiveData<UiState> = currentUiState.asLiveData2()

    fun onUserCancel() {
        Timber.d("onUserCancel()")
        postEvent(ShowCancelConfirmationDialog)
    }

    fun goBack() = postEvent(NavigateBack)

    fun showPrivacyInformation() = postEvent(NavigateToPrivacyInformation)

    fun onUserConsent(): Unit = launch {
        Timber.d("onUserConsent()")
        currentIsLoading.compareAndSet(expect = false, update = true)
        processUserConsent()
        currentIsLoading.compareAndSet(expect = true, update = false)
    }

    private suspend fun processUserConsent() = try {
        Timber.d("processUserConsent()")
        dccTicketingSharedViewModel.apply {
            transactionContext.first()
                .requestServiceIdentityDocumentOfValidationService()
                .generateECKeyPair()
                .requestAccessToken()
                .also { updateTransactionContext(ctx = it) }
        }
        postEvent(NavigateToCertificateSelection)
    } catch (e: DccTicketingException) {
        Timber.d(e, "Error while processing user consent")
        val serviceProvider = currentUiState.first().provider
        val lazyErrorMessage = e.errorMessage(serviceProvider = serviceProvider)
        postEvent(ShowErrorDialog(lazyErrorMessage = lazyErrorMessage))
    }

    private suspend fun DccTicketingTransactionContext.requestServiceIdentityDocumentOfValidationService():
        DccTicketingTransactionContext {
            Timber.d("requestServiceIdentityDocumentOfValidationService")

            requireNotNull(validationService) { "ctx.validationService must not be null" }
            requireNotNull(validationServiceJwkSet) { "ctx.validationServiceJwkSet must not be null" }

            val document = dccTicketingRequestService
                .requestValidationService(validationService, validationServiceJwkSet)

            return copy(
                validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC =
                document.validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC,
                validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM =
                document.validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM,
                validationServiceSignKeyJwkSet =
                document.validationServiceSignKeyJwkSet
            )
        }

    private fun DccTicketingTransactionContext.generateECKeyPair(): DccTicketingTransactionContext {
        Timber.d("generateECKeyPair()")

        val ecKeyPair = ecKeyGenerator.generateECKeyPair()

        return copy(
            ecPublicKey = ecKeyPair.publicKey,
            ecPrivateKey = ecKeyPair.privateKey,
            ecPublicKeyBase64 = ecKeyPair.publicKeyBase64
        )
    }

    private suspend fun DccTicketingTransactionContext.requestAccessToken(): DccTicketingTransactionContext {
        Timber.d("requestAccessToken()")

        requireNotNull(accessTokenService) { "ctx.accessTokenService must not be null" }
        requireNotNull(accessTokenServiceJwkSet) { "ctx.accessTokenServiceJwkSet must not be null" }
        requireNotNull(accessTokenSignJwkSet) { "ctx.accessTokenSignJwkSet must not be null" }
        requireNotNull(validationService) { "ctx.validationService must not be null" }
        requireNotNull(ecPublicKeyBase64) { "ctx.ecPublicKeyBase64 must not be null" }

        val accessToken = dccTicketingRequestService.requestAccessToken(
            accessTokenService = accessTokenService,
            accessTokenServiceJwkSet = accessTokenServiceJwkSet,
            accessTokenSignJwkSet = accessTokenSignJwkSet,
            authorization = initializationData.token,
            validationService = validationService,
            publicKeyBase64 = ecPublicKeyBase64
        )

        return copy(
            accessToken = accessToken.accessToken,
            accessTokenPayload = accessToken.accessTokenPayload,
            nonceBase64 = accessToken.nonceBase64
        )
    }

    private fun postEvent(event: DccTicketingConsentOneEvent) {
        Timber.d("postEvent(event=%s)", event)
        currentEvent.postValue(event)
    }

    data class UiState(
        private val dccTicketingTransactionContext: DccTicketingTransactionContext
    ) {
        val provider get() = dccTicketingTransactionContext.initializationData.serviceProvider
        val subject get() = dccTicketingTransactionContext.initializationData.subject
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<DccTicketingConsentOneViewModel> {
        fun create(
            dccTicketingSharedViewModel: DccTicketingSharedViewModel
        ): DccTicketingConsentOneViewModel
    }
}
