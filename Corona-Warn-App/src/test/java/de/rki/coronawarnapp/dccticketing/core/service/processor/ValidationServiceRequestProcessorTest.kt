package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
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

class ValidationServiceRequestProcessorTest : BaseTest() {

    @MockK lateinit var dccTicketingServer: DccTicketingServer

    private val instance: ValidationServiceRequestProcessor
        get() = ValidationServiceRequestProcessor(dccTicketingServer = dccTicketingServer)

    private val validationService = DccTicketingService(
        id = "id",
        type = "type",
        serviceEndpoint = "serviceEndpoint",
        name = "name"
    )

    private val validationServiceJwkSet = emptySet<DccJWK>()

    private val jwkRSAOAEPWithSHA256AESCBC = DccJWK(
        x5c = listOf("x5c"),
        kid = "kid",
        alg = "RSAOAEPWithSHA256AESCBC",
        use = DccJWK.Purpose.ENCRYPTION
    )

    private val jwkRSAOAEPWithSHA256AESGCM = jwkRSAOAEPWithSHA256AESCBC.copy(alg = "RSAOAEPWithSHA256AESGCM")

    private val jwkValidationServiceSignKey = jwkRSAOAEPWithSHA256AESCBC.copy(alg = "alg")

    private val verificationMethodRSAOAEPWithSHA256AESCBC = DccTicketingVerificationMethod(
        id = "verificationMethodRSAOAEPWithSHA256AESCBC/",
        type = "type",
        controller = "controller",
        publicKeyJwk = jwkRSAOAEPWithSHA256AESCBC,
        verificationMethods = null
    )

    private val verificationMethodRSAOAEPWithSHA256AESGCM = verificationMethodRSAOAEPWithSHA256AESCBC.copy(
        id = "verificationMethodRSAOAEPWithSHA256AESGCM",
        publicKeyJwk = jwkRSAOAEPWithSHA256AESGCM
    )

    private val validationServiceSignKey = verificationMethodRSAOAEPWithSHA256AESCBC.copy(
        id = "ValidationServiceSignKey-1",
        publicKeyJwk = jwkValidationServiceSignKey
    )

    private val verificationMethodsForRSAOAEPWithSHA256AESCBC = verificationMethodRSAOAEPWithSHA256AESCBC.copy(
        id = "ValidationServiceEncScheme-RSAOAEPWithSHA256AESCBC",
        publicKeyJwk = null,
        verificationMethods = listOf(verificationMethodRSAOAEPWithSHA256AESCBC.id)
    )

    private val verificationMethodsForRSAOAEPWithSHA256AESGCM = verificationMethodsForRSAOAEPWithSHA256AESCBC.copy(
        id = "ValidationServiceEncScheme-RSAOAEPWithSHA256AESGCM",
        verificationMethods = listOf(verificationMethodRSAOAEPWithSHA256AESGCM.id)
    )

    private val serviceIdentityDocument = DccTicketingServiceIdentityDocument(
        id = "id",
        verificationMethod = listOf(
            verificationMethodRSAOAEPWithSHA256AESGCM,
            verificationMethodRSAOAEPWithSHA256AESCBC,
            verificationMethodsForRSAOAEPWithSHA256AESCBC,
            verificationMethodsForRSAOAEPWithSHA256AESGCM,
            validationServiceSignKey
        ),
        _service = null
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `happy path`() = runBlockingTest {
        val validationServiceResult = ValidationServiceRequestProcessor.ValidationServiceResult(
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = setOf(jwkRSAOAEPWithSHA256AESCBC),
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = setOf(jwkRSAOAEPWithSHA256AESGCM),
            validationServiceSignKeyJwkSet = setOf(jwkValidationServiceSignKey)
        )

        checkResult(document = serviceIdentityDocument, result = validationServiceResult)

        coVerify {
            dccTicketingServer.getServiceIdentityDocumentAndValidateServerCert(
                url = validationService.serviceEndpoint,
                jwkSet = validationServiceJwkSet
            )
        }
    }

    @Test
    fun `happy path - only RSAOAEPWithSHA256AESCBC`() = runBlockingTest {
        val validationServiceResult = ValidationServiceRequestProcessor.ValidationServiceResult(
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = setOf(jwkRSAOAEPWithSHA256AESCBC),
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = emptySet(),
            validationServiceSignKeyJwkSet = setOf(jwkValidationServiceSignKey)
        )

        var document = serviceIdentityDocument.copy(
            verificationMethod = serviceIdentityDocument.verificationMethod - verificationMethodRSAOAEPWithSHA256AESGCM
        )

        checkResult(document = document, result = validationServiceResult)

        document = serviceIdentityDocument.copy(
            verificationMethod =
            serviceIdentityDocument.verificationMethod - verificationMethodsForRSAOAEPWithSHA256AESGCM
        )

        checkResult(document = document, result = validationServiceResult)
    }

    @Test
    fun `happy path - only RSAOAEPWithSHA256AESGCM`() = runBlockingTest {
        val validationServiceResult = ValidationServiceRequestProcessor.ValidationServiceResult(
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = emptySet(),
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = setOf(jwkRSAOAEPWithSHA256AESGCM),
            validationServiceSignKeyJwkSet = setOf(jwkValidationServiceSignKey)
        )

        var document = serviceIdentityDocument.copy(
            verificationMethod = serviceIdentityDocument.verificationMethod - verificationMethodRSAOAEPWithSHA256AESCBC
        )

        checkResult(document = document, result = validationServiceResult)

        document = serviceIdentityDocument.copy(
            verificationMethod =
            serviceIdentityDocument.verificationMethod - verificationMethodsForRSAOAEPWithSHA256AESCBC
        )

        checkResult(document = document, result = validationServiceResult)
    }

    @Test
    fun `throws if service identity document lacks encryption keys`() = runBlockingTest {
        val document = serviceIdentityDocument.copy(
            verificationMethod = emptyList()
        )

        coEvery {
            dccTicketingServer.getServiceIdentityDocumentAndValidateServerCert(
                url = any(),
                jwkSet = any()
            )
        } returns document

        shouldThrow<DccTicketingException> {
            instance.requestValidationService(
                validationService = validationService,
                validationServiceJwkSet = validationServiceJwkSet
            )
        }.errorCode shouldBe DccTicketingErrorCode.VS_ID_NO_ENC_KEY
    }

    @Test
    fun `throws if service identity document lacks required jwk set`() = runBlockingTest {
        val document = serviceIdentityDocument.copy(
            verificationMethod = serviceIdentityDocument.verificationMethod - validationServiceSignKey
        )

        coEvery {
            dccTicketingServer.getServiceIdentityDocumentAndValidateServerCert(
                url = any(),
                jwkSet = any()
            )
        } returns document

        shouldThrow<DccTicketingException> {
            instance.requestValidationService(
                validationService = validationService,
                validationServiceJwkSet = validationServiceJwkSet
            )
        }.errorCode shouldBe DccTicketingErrorCode.VS_ID_NO_SIGN_KEY
    }

    private suspend fun checkResult(
        document: DccTicketingServiceIdentityDocument,
        result: ValidationServiceRequestProcessor.ValidationServiceResult
    ) {
        coEvery {
            dccTicketingServer.getServiceIdentityDocumentAndValidateServerCert(
                url = any(),
                jwkSet = any()
            )
        } returns document

        instance.requestValidationService(
            validationService = validationService,
            validationServiceJwkSet = validationServiceJwkSet
        ) shouldBe result
    }

    @Test
    fun `Check server error mapping`() = runBlockingTest {
        with(instance) {
            checkServerErrorMapping(
                serverErrorCode = DccTicketingServerException.ErrorCode.PARSE_ERR,
                processorErrorCode = DccTicketingErrorCode.VS_ID_PARSE_ERR
            )
            checkServerErrorMapping(
                serverErrorCode = DccTicketingServerException.ErrorCode.SERVER_ERR,
                processorErrorCode = DccTicketingErrorCode.VS_ID_SERVER_ERR
            )
            checkServerErrorMapping(
                serverErrorCode = DccTicketingServerException.ErrorCode.CLIENT_ERR,
                processorErrorCode = DccTicketingErrorCode.VS_ID_CLIENT_ERR
            )
            checkServerErrorMapping(
                serverErrorCode = DccTicketingServerException.ErrorCode.NO_NETWORK,
                processorErrorCode = DccTicketingErrorCode.VS_ID_NO_NETWORK
            )

            checkServerErrorMapping(
                serverCertCheckErrorCode =
                DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID,
                processorErrorCode =
                DccTicketingErrorCode.VS_ID_CERT_PIN_NO_JWK_FOR_KID
            )
            checkServerErrorMapping(
                serverCertCheckErrorCode = DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_MISMATCH,
                processorErrorCode = DccTicketingErrorCode.VS_ID_CERT_PIN_MISMATCH
            )
        }
    }

    private suspend fun ValidationServiceRequestProcessor.checkServerErrorMapping(
        serverErrorCode: DccTicketingServerException.ErrorCode? = null,
        serverCertCheckErrorCode: DccTicketingServerCertificateCheckException.ErrorCode? = null,
        processorErrorCode: DccTicketingErrorCode
    ) {
        val serverException = when {
            serverErrorCode != null -> DccTicketingServerException(errorCode = serverErrorCode)
            serverCertCheckErrorCode != null ->
                DccTicketingServerCertificateCheckException(errorCode = serverCertCheckErrorCode)
            else -> null
        }

        coEvery {
            dccTicketingServer.getServiceIdentityDocumentAndValidateServerCert(
                url = any(),
                jwkSet = any()
            )
        } throws serverException!!

        shouldThrow<DccTicketingException> {
            requestValidationService(
                validationService = validationService,
                validationServiceJwkSet = validationServiceJwkSet
            )
        }.errorCode shouldBe processorErrorCode
    }
}
