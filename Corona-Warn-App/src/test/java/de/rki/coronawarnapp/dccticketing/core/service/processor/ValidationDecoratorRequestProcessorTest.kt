package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerException
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingVerificationMethod
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ValidationDecoratorRequestProcessorTest : BaseTest() {

    @MockK lateinit var dccTicketingServer: DccTicketingServer

    private val instance: ValidationDecoratorRequestProcessor
        get() = ValidationDecoratorRequestProcessor(dccTicketingServer = dccTicketingServer)

    private val url = "url"

    private val accessTokenServiceJWK = DccJWK(
        x5c = listOf("accessTokenServiceJWK"),
        kid = "kid",
        alg = "alg",
        use = DccJWK.Purpose.SIGNATURE
    )

    private val accessTokenServiceKeyJWK = accessTokenServiceJWK.copy(x5c = listOf("accessTokenServiceKeyJWK"))
    private val validationServiceKeyJWK = accessTokenServiceJWK.copy(x5c = listOf("validationServiceKeyJWK"))

    private val accessTokenSignKey = DccTicketingVerificationMethod(
        id = "AccessTokenSignKey-1",
        type = "type",
        controller = "controller",
        publicKeyJwk = accessTokenServiceJWK,
        verificationMethods = null
    )

    private val accessTokenServiceKey =
        accessTokenSignKey.copy(id = "AccessTokenServiceKey-1", publicKeyJwk = accessTokenServiceKeyJWK)
    private val validationServiceKey =
        accessTokenSignKey.copy(id = "ValidationServiceKey-1", publicKeyJwk = validationServiceKeyJWK)

    private val accessTokenService = DccTicketingService(
        id = "id",
        type = "AccessTokenService",
        serviceEndpoint = "serviceEndpoint",
        name = "name"
    )

    private val validationService = accessTokenService.copy(type = "ValidationService")

    private val serviceIdentityDocument = DccTicketingServiceIdentityDocument(
        id = "id",
        verificationMethod = listOf(accessTokenSignKey, accessTokenServiceKey, validationServiceKey),
        _service = listOf(accessTokenService, validationService)
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `happy path`() = runBlockingTest {
        coEvery { dccTicketingServer.getServiceIdentityDocument(any()) } returns serviceIdentityDocument

        val validationDecoratorResult = ValidationDecoratorRequestProcessor.ValidationDecoratorResult(
            accessTokenService = accessTokenService,
            accessTokenServiceJwkSet = setOf(accessTokenServiceKey.publicKeyJwk!!),
            accessTokenSignJwkSet = setOf(accessTokenSignKey.publicKeyJwk!!),
            validationService = validationService,
            validationServiceJwkSet = setOf(validationServiceKey.publicKeyJwk!!)
        )
        instance.requestValidationDecorator(url = url) shouldBe validationDecoratorResult

        coVerify { dccTicketingServer.getServiceIdentityDocument(url = url) }
    }

    @Test
    fun `throws if service identity document has faulty jwk`() = runBlockingTest {
        val faultyVerificationMethod = accessTokenServiceKey.copy(
            publicKeyJwk = accessTokenServiceKeyJWK.copy(x5c = emptyList())
        )
        val document = serviceIdentityDocument.copy(
            verificationMethod = serviceIdentityDocument.verificationMethod + faultyVerificationMethod
        )

        checkErrorCode(document = document, errorCode = DccTicketingErrorCode.VD_ID_EMPTY_X5C)
    }

    @Test
    fun `throws if service identity document lacks required service`() = runBlockingTest {
        checkServiceNotFoundErrorCode(service = accessTokenService, errorCode = DccTicketingErrorCode.VD_ID_NO_ATS)
        checkServiceNotFoundErrorCode(service = validationService, errorCode = DccTicketingErrorCode.VD_ID_NO_VS)
    }

    @Test
    fun `throws if service identity document lacks required jwk set`() = runBlockingTest {
        checkJwkNotFoundErrorCode(
            verificationMethod = accessTokenServiceKey,
            errorCode = DccTicketingErrorCode.VD_ID_NO_ATS_SVC_KEY
        )
        checkJwkNotFoundErrorCode(
            verificationMethod = accessTokenSignKey,
            errorCode = DccTicketingErrorCode.VD_ID_NO_ATS_SIGN_KEY
        )
        checkJwkNotFoundErrorCode(
            verificationMethod = validationServiceKey,
            errorCode = DccTicketingErrorCode.VD_ID_NO_VS_SVC_KEY
        )
    }

    @Test
    fun `Check server error mapping`() = runBlockingTest {
        with(instance) {
            checkServerErrorMapping(
                serverErrorCode = DccTicketingServerException.ErrorCode.PARSE_ERR,
                processorErrorCode = DccTicketingErrorCode.VD_ID_PARSE_ERR
            )
            checkServerErrorMapping(
                serverErrorCode = DccTicketingServerException.ErrorCode.SERVER_ERR,
                processorErrorCode = DccTicketingErrorCode.VD_ID_SERVER_ERR
            )
            checkServerErrorMapping(
                serverErrorCode = DccTicketingServerException.ErrorCode.CLIENT_ERR,
                processorErrorCode = DccTicketingErrorCode.VD_ID_CLIENT_ERR
            )
            checkServerErrorMapping(
                serverErrorCode = DccTicketingServerException.ErrorCode.NO_NETWORK,
                processorErrorCode = DccTicketingErrorCode.VD_ID_NO_NETWORK
            )
        }
    }

    private suspend fun ValidationDecoratorRequestProcessor.checkServerErrorMapping(
        serverErrorCode: DccTicketingServerException.ErrorCode,
        processorErrorCode: DccTicketingErrorCode
    ) {
        val serverException = DccTicketingServerException(errorCode = serverErrorCode)
        coEvery {
            dccTicketingServer.getServiceIdentityDocument(any())
        } throws serverException

        shouldThrow<DccTicketingException> {
            requestValidationDecorator(url = url)
        }.errorCode shouldBe processorErrorCode
    }

    private suspend fun checkErrorCode(
        document: DccTicketingServiceIdentityDocument,
        errorCode: DccTicketingErrorCode
    ) {
        coEvery { dccTicketingServer.getServiceIdentityDocument(any()) } returns document

        shouldThrow<DccTicketingException> {
            instance.requestValidationDecorator(url = url)
        }.errorCode shouldBe errorCode
    }

    private suspend fun checkServiceNotFoundErrorCode(service: DccTicketingService, errorCode: DccTicketingErrorCode) {
        val document = serviceIdentityDocument.copy(
            _service = serviceIdentityDocument.service - service
        )

        checkErrorCode(document = document, errorCode = errorCode)
    }

    private suspend fun checkJwkNotFoundErrorCode(
        verificationMethod: DccTicketingVerificationMethod,
        errorCode: DccTicketingErrorCode
    ) {
        var document = serviceIdentityDocument.copy(
            verificationMethod = serviceIdentityDocument.verificationMethod
                .minus(verificationMethod)
                .plus(verificationMethod.copy(id = ""))
        )

        checkErrorCode(document = document, errorCode = errorCode)

        document = serviceIdentityDocument.copy(
            verificationMethod = serviceIdentityDocument.verificationMethod
                .minus(verificationMethod)
                .plus(verificationMethod.copy(publicKeyJwk = null))
        )

        checkErrorCode(document = document, errorCode = errorCode)
    }
}
