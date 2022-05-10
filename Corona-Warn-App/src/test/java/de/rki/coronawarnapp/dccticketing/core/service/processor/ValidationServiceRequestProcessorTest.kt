package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateCheckException
import de.rki.coronawarnapp.dccticketing.core.check.DccTicketingServerCertificateChecker
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServer
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerException
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerParser
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingVerificationMethod
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest

class ValidationServiceRequestProcessorTest : BaseTest() {

    @MockK lateinit var dccTicketingServer: DccTicketingServer
    @RelaxedMockK lateinit var dccTicketingServerCertificateChecker: DccTicketingServerCertificateChecker
    @MockK lateinit var dccTicketingServerParser: DccTicketingServerParser

    private val instance: ValidationServiceRequestProcessor
        get() = ValidationServiceRequestProcessor(
            dccTicketingServer = dccTicketingServer,
            serverCertificateChecker = dccTicketingServerCertificateChecker,
            dccTicketingServerParser = dccTicketingServerParser
        )

    val response: Response<ResponseBody> = mockk {
        every { raw() } returns mockk()
    }

    private val validationService = DccTicketingService(
        id = "id",
        type = "type",
        serviceEndpoint = "serviceEndpoint",
        name = "name"
    )

    private val validationServiceJwkSet = emptySet<DccJWK>()
    private val validationAllowlist = emptySet<DccTicketingValidationServiceAllowListEntry>()

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

        coEvery { dccTicketingServer.getServiceIdentityDocument(any()) } returns response
    }

    @Test
    fun `happy path`() = runTest {
        val validationServiceResult = ValidationServiceRequestProcessor.ValidationServiceResult(
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC = setOf(jwkRSAOAEPWithSHA256AESCBC),
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = setOf(jwkRSAOAEPWithSHA256AESGCM),
            validationServiceSignKeyJwkSet = setOf(jwkValidationServiceSignKey)
        )

        checkResult(document = serviceIdentityDocument, result = validationServiceResult)

        coVerify {
            dccTicketingServer.getServiceIdentityDocument(url = validationService.serviceEndpoint)
            dccTicketingServerParser.createServiceIdentityDocument(response = response)
        }
    }

    @Test
    fun `happy path - only RSAOAEPWithSHA256AESCBC`() = runTest {
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
    fun `happy path - only RSAOAEPWithSHA256AESGCM`() = runTest {
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
    fun `throws if service identity document lacks encryption keys`() = runTest {
        val document = serviceIdentityDocument.copy(
            verificationMethod = emptyList()
        )

        coEvery {
            dccTicketingServerParser.createServiceIdentityDocument(any())
        } returns document

        shouldThrow<DccTicketingException> {
            instance.requestValidationService(
                validationService = validationService,
                validationServiceJwkSet = validationServiceJwkSet,
                validationServiceAllowList = validationAllowlist
            )
        }.errorCode shouldBe DccTicketingErrorCode.VS_ID_NO_ENC_KEY
    }

    @Test
    fun `throws if service identity document lacks required jwk set`() = runTest {
        val document = serviceIdentityDocument.copy(
            verificationMethod = serviceIdentityDocument.verificationMethod - validationServiceSignKey
        )

        coEvery {
            dccTicketingServerParser.createServiceIdentityDocument(any())
        } returns document

        shouldThrow<DccTicketingException> {
            instance.requestValidationService(
                validationService = validationService,
                validationServiceJwkSet = validationServiceJwkSet,
                validationServiceAllowList = validationAllowlist
            )
        }.errorCode shouldBe DccTicketingErrorCode.VS_ID_NO_SIGN_KEY
    }

    @Test
    fun `throws if parser throws`() = runTest {
        coEvery {
            dccTicketingServerParser.createServiceIdentityDocument(any())
        } throws DccTicketingServerException(errorCode = DccTicketingServerException.ErrorCode.PARSE_ERR)

        shouldThrow<DccTicketingException> {
            instance.requestValidationService(validationService, validationServiceJwkSet, validationAllowlist)
        }.errorCode shouldBe DccTicketingErrorCode.VS_ID_PARSE_ERR
    }

    @Test
    fun `Check server error mapping`() = runTest {
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
                DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_HOST_MISMATCH,
                processorErrorCode =
                DccTicketingErrorCode.VS_ID_CERT_PIN_HOST_MISMATCH
            )

            checkServerErrorMapping(
                serverCertCheckErrorCode =
                DccTicketingServerCertificateCheckException.ErrorCode.CERT_PIN_NO_JWK_FOR_KID,
                processorErrorCode =
                DccTicketingErrorCode.VS_ID_CERT_PIN_HOST_MISMATCH
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
            dccTicketingServer.getServiceIdentityDocument(any())
        } throws serverException!!

        shouldThrow<DccTicketingException> {
            requestValidationService(
                validationService = validationService,
                validationServiceJwkSet = validationServiceJwkSet,
                validationServiceAllowList = validationAllowlist
            )
        }.errorCode shouldBe processorErrorCode
    }

    private suspend fun checkResult(
        document: DccTicketingServiceIdentityDocument,
        result: ValidationServiceRequestProcessor.ValidationServiceResult
    ) {
        coEvery {
            dccTicketingServerParser.createServiceIdentityDocument(any())
        } returns document

        instance.requestValidationService(
            validationService = validationService,
            validationServiceJwkSet = validationServiceJwkSet,
            validationServiceAllowList = validationAllowlist
        ) shouldBe result
    }
}
