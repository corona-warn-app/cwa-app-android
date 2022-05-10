package de.rki.coronawarnapp.dccticketing.core.service

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.service.processor.AccessTokenRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenInput
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenOutput
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationDecoratorRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationServiceRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

class DccTicketingRequestServiceTest : BaseTest() {

    @MockK lateinit var validationServiceRequestProcessor: ValidationServiceRequestProcessor
    @MockK lateinit var validationDecoratorRequestProcessor: ValidationDecoratorRequestProcessor
    @MockK lateinit var accessTokenRequestProcessor: AccessTokenRequestProcessor
    @MockK lateinit var resultTokenRequestProcessor: ResultTokenRequestProcessor

    private val instance: DccTicketingRequestService
        get() = DccTicketingRequestService(
            dispatcherProvider = TestDispatcherProvider(),
            validationDecoratorRequestProcessor = validationDecoratorRequestProcessor,
            validationServiceRequestProcessor = validationServiceRequestProcessor,
            accessTokenRequestProcessor = accessTokenRequestProcessor,
            resultTokenRequestProcessor = resultTokenRequestProcessor
        )

    private val dccTicketingService = DccTicketingService(
        id = "id",
        type = "type",
        serviceEndpoint = "serviceEndpoint",
        name = "name"
    )

    private val validationServiceResult = ValidationServiceRequestProcessor.ValidationServiceResult(
        validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = emptySet(),
        validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = emptySet(),
        validationServiceSignKeyJwkSet = emptySet()
    )

    private val validationDecoratorResult = ValidationDecoratorRequestProcessor.ValidationDecoratorResult(
        accessTokenService = dccTicketingService,
        accessTokenServiceJwkSet = emptySet(),
        accessTokenSignJwkSet = emptySet(),
        validationService = dccTicketingService,
        validationServiceJwkSet = emptySet()
    )

    private val accessTokenResult = AccessTokenRequestProcessor.Output(
        accessToken = "accessToken",
        accessTokenPayload = mockk(),
        nonceBase64 = "nonceBase64"
    )

    private val resultTokenOutput = ResultTokenOutput(
        resultToken = "resultToken",
        resultTokenPayload = mockk()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery {
            validationServiceRequestProcessor.requestValidationService(
                any(),
                any(),
                any()
            )
        } returns validationServiceResult

        coEvery {
            validationDecoratorRequestProcessor.requestValidationDecorator(any(), any())
        } returns validationDecoratorResult

        coEvery {
            accessTokenRequestProcessor.requestAccessToken(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns accessTokenResult

        coEvery { resultTokenRequestProcessor.requestResultToken(any()) } returns resultTokenOutput
    }

    @Test
    fun `requestValidationDecorator() - forwards call to processor`() = runTest {
        val url = "serviceEndpoint"
        val validationServiceAllowList = setOf<DccTicketingValidationServiceAllowListEntry>()

        with(instance) {
            requestValidationDecorator(url, validationServiceAllowList) shouldBe validationDecoratorResult

            coVerify { validationDecoratorRequestProcessor.requestValidationDecorator(url, validationServiceAllowList) }

            val error = DccTicketingException(errorCode = DccTicketingErrorCode.VD_ID_SERVER_ERR)
            coEvery { validationDecoratorRequestProcessor.requestValidationDecorator(any(), any()) } throws error

            shouldThrow<DccTicketingException> {
                requestValidationDecorator(
                    url,
                    validationServiceAllowList
                )
            } shouldBe error
        }
    }

    @Test
    fun `requestValidationService() - forwards call to processor`() = runTest {
        val validationService = dccTicketingService
        val validationServiceJwkSet = emptySet<DccJWK>()
        val validationServiceAllowList = emptySet<DccTicketingValidationServiceAllowListEntry>()

        with(instance) {
            requestValidationService(
                validationService = validationService,
                validationServiceJwkSet = validationServiceJwkSet,
                validationServiceAllowList = validationServiceAllowList
            ) shouldBe validationServiceResult

            coVerify {
                validationServiceRequestProcessor.requestValidationService(
                    validationService = validationService,
                    validationServiceJwkSet = validationServiceJwkSet,
                    validationServiceAllowList = validationServiceAllowList
                )
            }

            val error = DccTicketingException(errorCode = DccTicketingErrorCode.VS_ID_SERVER_ERR)
            coEvery {
                validationServiceRequestProcessor.requestValidationService(
                    validationService = any(),
                    validationServiceJwkSet = any(),
                    validationServiceAllowList = any()
                )
            } throws error

            shouldThrow<DccTicketingException> {
                requestValidationService(
                    validationService = validationService,
                    validationServiceJwkSet = validationServiceJwkSet,
                    validationServiceAllowList = validationServiceAllowList
                )
            } shouldBe error
        }
    }

    @Test
    fun `requestAccessToken() - forwards call to processor`() = runTest {
        val accessTokenService = dccTicketingService
        val accessTokenServiceJwkSet = emptySet<DccJWK>()
        val accessTokenSignJwkSet = emptySet<DccJWK>()
        val validationService = dccTicketingService
        val publicKeyBase64 = "publicKeyBase64"
        val authorization = "authorization"

        with(instance) {
            requestAccessToken(
                accessTokenService = accessTokenService,
                accessTokenServiceJwkSet = accessTokenServiceJwkSet,
                accessTokenSignJwkSet = accessTokenSignJwkSet,
                validationService = validationService,
                publicKeyBase64 = publicKeyBase64,
                authorization = authorization
            ) shouldBe accessTokenResult

            coVerify {
                accessTokenRequestProcessor.requestAccessToken(
                    accessTokenService = accessTokenService,
                    accessTokenServiceJwkSet = accessTokenServiceJwkSet,
                    accessTokenSignJwkSet = accessTokenSignJwkSet,
                    validationService = validationService,
                    publicKeyBase64 = publicKeyBase64,
                    authorization = authorization
                )
            }

            val error = DccTicketingException(errorCode = DccTicketingErrorCode.ATR_SERVER_ERR)
            coEvery {
                accessTokenRequestProcessor.requestAccessToken(
                    accessTokenService = any(),
                    accessTokenServiceJwkSet = any(),
                    accessTokenSignJwkSet = any(),
                    validationService = any(),
                    publicKeyBase64 = any(),
                    authorization = any()
                )
            } throws error

            shouldThrow<DccTicketingException> {
                requestAccessToken(
                    accessTokenService = accessTokenService,
                    accessTokenServiceJwkSet = accessTokenServiceJwkSet,
                    accessTokenSignJwkSet = accessTokenSignJwkSet,
                    validationService = validationService,
                    publicKeyBase64 = publicKeyBase64,
                    authorization = authorization
                )
            } shouldBe error
        }
    }

    @Test
    fun `requestResultToken() - forwards call to processor`() = runTest {
        val input = ResultTokenInput(
            serviceEndpoint = "serviceEndpoint",
            validationServiceJwkSet = emptySet(),
            validationServiceSignKeyJwkSet = emptySet(),
            jwt = "jwt",
            encryptionKeyKid = "encryptionKeyKid",
            encryptedDCCBase64 = "encryptedDCCBase64",
            encryptionKeyBase64 = "encryptionKeyBase64",
            signatureBase64 = "signatureBase64",
            signatureAlgorithm = "signatureAlgorithm",
            encryptionScheme = "encryptionScheme",
            allowlist = emptySet()
        )

        with(instance) {
            requestResultToken(resultTokenInput = input) shouldBe resultTokenOutput

            coVerify { resultTokenRequestProcessor.requestResultToken(resultTokenInput = input) }

            val error = DccTicketingException(errorCode = DccTicketingErrorCode.RTR_SERVER_ERR)
            coEvery { resultTokenRequestProcessor.requestResultToken(resultTokenInput = any()) } throws error

            shouldThrow<DccTicketingException> { requestResultToken(resultTokenInput = input) } shouldBe error
        }
    }
}
