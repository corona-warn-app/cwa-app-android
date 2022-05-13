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
import de.rki.coronawarnapp.util.encryption.ec.ECKeyPair
import de.rki.coronawarnapp.util.encryption.ec.EcKeyGenerator
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccTicketingConsentOneProcessorTest : BaseTest() {

    @MockK private lateinit var dccTicketingRequestService: DccTicketingRequestService
    @MockK private lateinit var ecKeyGenerator: EcKeyGenerator

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
        validationServiceJwkSet = validationDecoratorResult.validationServiceJwkSet,
        allowlist = emptySet()
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

    private val instance
        get() = DccTicketingConsentOneProcessor(
            dccTicketingRequestService = dccTicketingRequestService,
            ecKeyGenerator = ecKeyGenerator
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        dccTicketingRequestService.apply {
            coEvery { requestValidationService(any(), any(), any()) } returns validationServiceResult
            coEvery { requestAccessToken(any(), any(), any(), any(), any(), any()) } returns accessTokenOutput
        }

        every { ecKeyGenerator.generateECKeyPair() } returns ecKeyPair
    }

    @Test
    fun `updateTransactionContext - happy path`() = runTest {
        instance.updateTransactionContext(ctx = transactionContext) shouldBe transactionContext.copy(
            accessTokenService = validationDecoratorResult.accessTokenService,
            accessTokenServiceJwkSet = validationDecoratorResult.accessTokenServiceJwkSet,
            accessTokenSignJwkSet = validationDecoratorResult.accessTokenSignJwkSet,
            validationService = validationDecoratorResult.validationService,
            validationServiceJwkSet = validationDecoratorResult.validationServiceJwkSet,
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
            nonceBase64 = accessTokenOutput.nonceBase64,
            allowlist = emptySet()
        )
    }

    @Test
    fun `Forwards errors`() = runTest {
        val genericError = Exception("Test Error")
        val dccTicketingException = DccTicketingException(errorCode = DccTicketingException.ErrorCode.VS_ID_EMPTY_X5C)

        every { ecKeyGenerator.generateECKeyPair() } throws genericError

        instance.runCatching {
            shouldThrow<Exception> {
                updateTransactionContext(transactionContext)
            } shouldBe genericError

            coEvery {
                dccTicketingRequestService.requestValidationService(
                    any(),
                    any(),
                    any()
                )
            } throws dccTicketingException

            shouldThrow<Exception> {
                updateTransactionContext(transactionContext)
            } shouldBe dccTicketingException
        }
    }

    @Test
    fun `Throws if required values are not set`() = runTest {
        instance.run {
            shouldThrow<IllegalArgumentException> {
                updateTransactionContext(
                    ctx = transactionContext.copy(
                        validationService = null
                    )
                )
            }

            shouldNotThrowAny {
                updateTransactionContext(ctx = transactionContext)
            }
        }
    }
}
