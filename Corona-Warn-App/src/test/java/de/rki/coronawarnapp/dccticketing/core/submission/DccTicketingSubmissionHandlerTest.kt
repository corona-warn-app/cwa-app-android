package de.rki.coronawarnapp.dccticketing.core.submission

import de.rki.coronawarnapp.dccticketing.core.common.DccJWKConverter
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.VS_ID_CLIENT_ERR
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException.ErrorCode.VS_ID_NO_ENC_KEY
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.security.DccTicketingSecurityTool
import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.service.processor.ResultTokenOutput
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultItem
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.util.security.Sha256Signature
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.PrivateKey
import java.security.PublicKey

@Suppress("MaxLineLength")
class DccTicketingSubmissionHandlerTest : BaseTest() {

    lateinit var handler: DccTicketingSubmissionHandler

    @MockK lateinit var securityTool: DccTicketingSecurityTool
    @MockK lateinit var converter: DccJWKConverter
    @MockK lateinit var requestService: DccTicketingRequestService
    @MockK lateinit var publicKey: PublicKey
    @MockK lateinit var privateKey: PrivateKey

    private val dccJWK = DccJWK(
        x5c = listOf("MIIBtzCCAV6gAwIBAgIJANocmV/U2sWrMAkGByqGSM49BAEwYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMB4XDTIxMTAyODEwMDUyMloXDTMxMTAyNjEwMDUyMlowYjELMAkGA1UEBhMCREUxCzAJBgNVBAgMAkJXMREwDwYDVQQHDAhXYWxsZG9yZjEPMA0GA1UECgwGU0FQIFNFMRAwDgYDVQQLDAdDV0EgQ0xJMRAwDgYDVQQDDAdjd2EtY2xpMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEQIMN3/9b32RulED0quAjfqbJ161DMlEX0vKmRJeKkF9qSvGDh54wY3wvEGzR8KRoIkltp2/OwqUWNCzE3GDDbjAJBgcqhkjOPQQBA0gAMEUCIBGdBhvrxWHgXAidJbNEpbLyOrtgynzS9m9LGiCWvcpsAiEAjeJvDQ03n6NR8ZauecRtxTyXzFx8lv6XA273K05COpI="),
        kid = "pGWqzB9BzWY=",
        alg = "ES256",
        use = DccJWK.Purpose.SIGNATURE
    )

    private val transactionContext = DccTicketingTransactionContext(
        initializationData = DccTicketingQrCodeData(
            protocol = "protocol",
            protocolVersion = "1",
            serviceIdentity = "service identity",
            privacyUrl = "privacy url",
            consent = "consent",
            serviceProvider = "service provider",
            subject = "subject",
            token = "token",
        )
    )

    private val output = DccTicketingSecurityTool.Output(
        encryptedDCCBase64 = "encryptedDCCBase64",
        encryptionKeyBase64 = "encryptionKeyBase64",
        signatureBase64 = "signatureBase64",
        signatureAlgorithm = Sha256Signature.ALGORITHM
    )

    private val jwtTokenObject = DccTicketingAccessToken(
        iss = "https://dgca-booking-demo-eu-test.cfapps.eu10.hana.ondemand.com/api/identity",
        exp = 1635840944,
        sub = "958a3eb2-503c-45af-a855-4febb47586b2",
        aud = "https://dgca-validation-service-eu-acc.cfapps.eu10.hana.ondemand.com/validate/958a3eb2-503c-45af-a855-4febb47586b2",
        t = 2,
        v = "1.0",
        iat = 1635837344,
        jti = "adddda56-a7d2-4657-a395-c8b2dd3f5264",
        vc = DccTicketingValidationCondition(
            lang = "en-en",
            fnt = "WURST",
            gnt = "HANS",
            dob = "1990-01-01",
            coa = "AF",
            cod = "SJ",
            roa = "AF",
            rod = "SJ",
            type = listOf(
                "r",
                "v",
                "t"
            ),
            category = listOf(
                "Standard"
            ),
            validationClock = "2021-11-03T15:39:43+00:00",
            validFrom = "2021-11-03T07:15:43+00:00",
            validTo = "2021-11-03T15:39:43+00:00",
            hash = null
        ),
    )

    private val resultToken = DccTicketingResultToken(
        iss = "https://dgca-validation-service-eu-acc.cfapps.eu10.hana.ondemand.com",
        sub = "1044236f-48df-43cb-8bdf-bed142e507ab",
        iat = 1635864502,
        exp = 1635950902,
        category = listOf("Standard"),
        confirmation = "eyJraWQiOiJSQU0yU3R3N0VrRT0iLCJhbGciOiJFUzI1NiJ9.eyJqdGkiOiJlMWU2YjU4MS1lN2NmLTQyZTAtYjM1ZS1jZmFhMTRkZTcxN2UiLCJzdWIiOiIxMDQ0MjM2Zi00OGRmLTQzY2ItOGJkZi1iZWQxNDJlNTA3YWIiLCJpc3MiOiJodHRwczovL2RnY2EtdmFsaWRhdGlvbi1zZXJ2aWNlLWV1LWFjYy5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbSIsImlhdCI6MTYzNTg2NDUwMiwiZXhwIjoxNjM1OTUwOTAyLCJyZXN1bHQiOiJOT0siLCJjYXRlZ29yeSI6WyJTdGFuZGFyZCJdfQ.OLnS59EWkpkZoEMfbyOs18dUauch9eaXxGK8Zrn-jo-S1kcgAxP8z8rdzLzNjCNTfi4CbVUnF6FV0lHuMnYBOw",
        result = DccTicketingResultToken.DccResult.FAIL,
        results = listOf(
            DccTicketingResultItem(
                identifier = "KID",
                result = DccTicketingResultToken.DccResult.FAIL,
                type = "TechnicalVerification",
                details = "\"unknown dcc signing kid\""
            )
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        handler = DccTicketingSubmissionHandler(
            securityTool,
            converter,
            requestService
        )
    }

    @Test
    fun `happy path`() {
        val ctx = transactionContext.copy(
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = setOf(dccJWK),
            dccBarcodeData = "dccBarcodeData",
            nonceBase64 = "nonceBase64",
            encryptedDCCBase64 = "encryptedDCCBase64",
            encryptionKeyBase64 = "encryptionKeyBase64",
            signatureBase64 = "signatureBase64",
            signatureAlgorithm = "signatureAlgorithm",
            validationServiceJwkSet = setOf(dccJWK),
            validationServiceSignKeyJwkSet = setOf(dccJWK),
            ecPrivateKey = privateKey,
            accessTokenPayload = jwtTokenObject,
            accessToken = "access token",
            allowlist = emptySet()
        )
        every { converter.createPublicKey(any()) } returns publicKey
        every { securityTool.encryptAndSign(any()) } returns output
        coEvery { requestService.requestResultToken(any()) } returns ResultTokenOutput(
            resultToken = "resultToken",
            resultTokenPayload = resultToken
        )
        runTest {
            shouldNotThrowAny {
                handler.submitDcc(ctx)
            }
        }
    }

    @Test
    fun `missing encryption key throws exception`() {
        runTest {
            shouldThrow<DccTicketingException> {
                handler.submitDcc(transactionContext)
            }.errorCode shouldBe VS_ID_NO_ENC_KEY
        }
    }

    @Test
    fun `missing context attributes throw exception`() {
        val ctx = transactionContext.copy(
            validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM = setOf(dccJWK)
        )
        every { converter.createPublicKey(any()) } returns publicKey
        runTest {
            shouldThrow<DccTicketingException> {
                handler.submitDcc(ctx)
            }.errorCode shouldBe VS_ID_CLIENT_ERR
        }
    }
}
