package de.rki.coronawarnapp.dccticketing.ui.consent.one

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.service.processor.AccessTokenRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationDecoratorRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationServiceRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import de.rki.coronawarnapp.util.encryption.ec.ECKeyPair
import de.rki.coronawarnapp.util.encryption.ec.EcKeyGenerator
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class DccTicketingConsentOneViewModelTest : BaseTest() {

    @MockK private lateinit var dccTicketingSharedViewModel: DccTicketingSharedViewModel
    @MockK private lateinit var dccTicketingRequestService: DccTicketingRequestService
    @MockK private lateinit var ecKeyGenerator: EcKeyGenerator

    private val mutableTransactionContextFlow = MutableStateFlow<DccTicketingTransactionContext?>(null)

    private val data = DccTicketingQrCodeData(
        protocol = "Test protocol",
        protocolVersion = "Test protocolVersion",
        serviceIdentity = "Test serviceIdentity",
        privacyUrl = "https://www.test.de/",
        token = "Test token",
        consent = "Test consent",
        subject = "Test subject",
        serviceProvider = "Test serviceProvider"
    )

    private val validationService = DccTicketingService(
        id = "Test id",
        type = "Test type Validation",
        serviceEndpoint = "https://www.test.de/serviceEndpoint",
        name = "Test Validation Service"
    )

    private val validationDecoratorResult = ValidationDecoratorRequestProcessor.ValidationDecoratorResult(
        accessTokenService = validationService.copy(name = "Test Access Service", type = "Test type Access"),
        accessTokenServiceJwkSet = emptySet(),
        accessTokenSignJwkSet = emptySet(),
        validationService = validationService,
        validationServiceJwkSet = emptySet()
    )
    private val transactionContext = DccTicketingTransactionContext(
        initializationData = data,
        accessTokenService = validationDecoratorResult.accessTokenService,
        accessTokenServiceJwkSet = validationDecoratorResult.accessTokenServiceJwkSet,
        accessTokenSignJwkSet = validationDecoratorResult.accessTokenSignJwkSet,
        validationService = validationDecoratorResult.validationService,
        validationServiceJwkSet = validationDecoratorResult.validationServiceJwkSet
    )

    private val validationServiceResult = ValidationServiceRequestProcessor.ValidationServiceResult(
        validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = emptySet(),
        validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = emptySet(),
        validationServiceSignKeyJwkSet = emptySet()
    )

    private val accessToken = DccTicketingAccessToken(
        iss = "Test iss",
        iat = 0,
        exp = 0,
        sub = "Test sub",
        aud = "Test aud",
        jti = "Test jti",
        v = "Test v",
        t = 0,
        vc = null
    )

    private val accessTokenOutput = AccessTokenRequestProcessor.Output(
        accessToken = "Test accessToken",
        accessTokenPayload = accessToken,
        nonceBase64 = "Test nonceBase64"
    )

    private val ecKeyPair = ECKeyPair(
        publicKey = mockk(),
        privateKey = mockk(),
        publicKeyBase64 = "Test publicKeyBase64"
    )

    private val instance: DccTicketingConsentOneViewModel
        get() = DccTicketingConsentOneViewModel(
            dccTicketingSharedViewModel = dccTicketingSharedViewModel,
            dispatcherProvider = TestDispatcherProvider(),
            dccTicketingRequestService = dccTicketingRequestService,
            ecKeyGenerator = ecKeyGenerator
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mutableTransactionContextFlow.value = null
        dccTicketingSharedViewModel.apply {
            every { transactionContext } returns mutableTransactionContextFlow.filterNotNull()
            every { updateTransactionContext(any()) } answers { mutableTransactionContextFlow.value = arg(0) }
        }

        dccTicketingRequestService.apply {
            coEvery { requestValidationService(any(), any()) } returns validationServiceResult
            coEvery { requestAccessToken(any(), any(), any(), any(), any(), any()) } returns accessTokenOutput
        }

        every { ecKeyGenerator.generateECKeyPair() } returns ecKeyPair
    }

    @Test
    fun `onConsent() - happy path`() = runBlockingTest2(ignoreActive = true) {
        mutableTransactionContextFlow.value = transactionContext

        instance.onUserConsent()

        mutableTransactionContextFlow.value shouldBe transactionContext.copy(
            accessTokenService = validationDecoratorResult.accessTokenService,
            accessTokenServiceJwkSet = validationDecoratorResult.accessTokenServiceJwkSet,
            accessTokenSignJwkSet = validationDecoratorResult.accessTokenSignJwkSet,
            validationService = validationDecoratorResult.validationService,
            validationServiceJwkSet = emptySet(),
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC =
            validationServiceResult.validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC,
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM =
            validationServiceResult.validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM,
            validationServiceSignKeyJwkSet = validationServiceResult.validationServiceSignKeyJwkSet,
            ecPublicKey = ecKeyPair.publicKey,
            ecPrivateKey = ecKeyPair.privateKey,
            ecPublicKeyBase64 = ecKeyPair.publicKeyBase64,
            accessToken = accessTokenOutput.accessToken,
            accessTokenPayload = accessTokenOutput.accessTokenPayload,
            nonceBase64 = accessTokenOutput.nonceBase64
        )
    }

    @Test
    fun `goBack() - NavigateBack`() {
        instance.run {
            goBack()
            events.getOrAwaitValue() shouldBe NavigateBack
        }
    }

    @Test
    fun `onUserCancel() - ShowCancelConfirmationDialog`() {
        instance.run {
            onUserCancel()
            events.getOrAwaitValue() shouldBe ShowCancelConfirmationDialog
        }
    }

    @Test
    fun `showPrivacyInformation() - NavigateToPrivacyInformation`() {
        instance.run {
            showPrivacyInformation()
            events.getOrAwaitValue() shouldBe NavigateToPrivacyInformation
        }
    }

    @Test
    fun `on DccTicketingException - ShowErrorDialog`() {
        mutableTransactionContextFlow.value = transactionContext

        coEvery { dccTicketingRequestService.requestValidationService(any(), any()) } throws DccTicketingException(
            errorCode = DccTicketingException.ErrorCode.VS_ID_CERT_PIN_NO_JWK_FOR_KID
        )

        instance.run {
            onUserConsent()
            events.getOrAwaitValue() should beInstanceOf<ShowErrorDialog>()
        }
    }
}
