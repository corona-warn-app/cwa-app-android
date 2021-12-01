package de.rki.coronawarnapp.dccticketing.core.service

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.service.processor.AccessTokenRequestProcessor
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
import kotlinx.coroutines.test.runBlockingTest
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

    private val validationServiceResult = ValidationServiceRequestProcessor.ValidationServiceResult(
        validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = emptySet(),
        validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = emptySet(),
        validationServiceSignKeyJwkSet = emptySet()
    )

    private val validationDecoratorResult = ValidationDecoratorRequestProcessor.ValidationDecoratorResult(
        accessTokenService = mockk(),
        accessTokenServiceJwkSet = emptySet(),
        accessTokenSignJwkSet = emptySet(),
        validationService = mockk(),
        validationServiceJwkSet = emptySet()
    )

    private val accessTokenResult = AccessTokenRequestProcessor.Output(
        accessToken = "accessToken",
        accessTokenPayload = mockk(),
        nonceBase64 = "nonceBase64"
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery {
            validationServiceRequestProcessor.requestValidationService(
                any(),
                any()
            )
        } returns validationServiceResult

        coEvery {
            validationDecoratorRequestProcessor.requestValidationDecorator(any())
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
    }

    @Test
    fun `requestValidationDecorator() - forwards call to processor`() = runBlockingTest {
        val url = "https://test.com"

        with(instance) {
            requestValidationDecorator(url) shouldBe validationDecoratorResult

            coVerify { validationDecoratorRequestProcessor.requestValidationDecorator(url) }

            val error = DccTicketingException(errorCode = DccTicketingErrorCode.VD_ID_SERVER_ERR)
            coEvery { validationDecoratorRequestProcessor.requestValidationDecorator(any()) } throws error

            shouldThrow<DccTicketingException> { requestValidationDecorator(url) } shouldBe error
        }
    }

    @Test
    fun `requestValidationService() - forwards call to processor`() = runBlockingTest {
        val validationService = mockk<DccTicketingService>()
        val validationServiceJwkSet = emptySet<DccJWK>()

        with(instance) {
            requestValidationService(
                validationService = validationService,
                validationServiceJwkSet = validationServiceJwkSet
            ) shouldBe validationServiceResult

            coVerify {
                validationServiceRequestProcessor.requestValidationService(
                    validationService = validationService,
                    validationServiceJwkSet = validationServiceJwkSet
                )
            }

            val error = DccTicketingException(errorCode = DccTicketingErrorCode.VS_ID_SERVER_ERR)
            coEvery {
                validationServiceRequestProcessor.requestValidationService(
                    validationService = any(),
                    validationServiceJwkSet = any()
                )
            } throws error

            shouldThrow<DccTicketingException> {
                requestValidationService(
                    validationService = validationService,
                    validationServiceJwkSet = validationServiceJwkSet
                )
            } shouldBe error
        }
    }

    @Test
    fun `requestAccessToken() - forwards call to processor`() = runBlockingTest {
        val accessTokenService = mockk<DccTicketingService>()
        val accessTokenServiceJwkSet = emptySet<DccJWK>()
        val accessTokenSignJwkSet = emptySet<DccJWK>()
        val validationService = mockk<DccTicketingService>()
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
}
