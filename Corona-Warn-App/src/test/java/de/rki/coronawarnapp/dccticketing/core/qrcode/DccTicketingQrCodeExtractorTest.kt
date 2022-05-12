package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingInvalidQrCodeException.ErrorCode.INIT_DATA_PARSE_ERR
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingInvalidQrCodeException.ErrorCode.INIT_DATA_PROTOCOL_INVALID
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingInvalidQrCodeException.ErrorCode.INIT_DATA_SP_EMPTY
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingInvalidQrCodeException.ErrorCode.INIT_DATA_SUBJECT_EMPTY
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccTicketingQrCodeExtractorTest : BaseTest() {

    @Inject lateinit var extractor: DccTicketingQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @Test
    fun `happy path extraction`() = runTest {
        shouldNotThrowAny {
            extractor.canHandle(validQrCode) shouldBe true
            extractor.extract(validQrCode)
        }
    }

    @Test
    fun `missing service provider throws exception`() = runTest {
        extractor.canHandle(invalidQrCodeMissingServiceProvider) shouldBe true
        shouldThrow<DccTicketingInvalidQrCodeException> {
            extractor.extract(invalidQrCodeMissingServiceProvider)
        }.errorCode shouldBe INIT_DATA_SP_EMPTY
    }

    @Test
    fun `wrong protocol throws exception`() = runTest {
        extractor.canHandle(invalidQrCodeWrongProtocol) shouldBe true
        shouldThrow<DccTicketingInvalidQrCodeException> {
            extractor.extract(invalidQrCodeWrongProtocol)
        }.errorCode shouldBe INIT_DATA_PROTOCOL_INVALID
    }

    @Test
    fun `missing subject throws exception`() = runTest {
        extractor.canHandle(invalidQrCodeMissingSubject) shouldBe true
        shouldThrow<DccTicketingInvalidQrCodeException> {
            extractor.extract(invalidQrCodeMissingSubject)
        }.errorCode shouldBe INIT_DATA_SUBJECT_EMPTY
    }

    @Test
    fun `malformed json throws exception`() = runTest {
        extractor.canHandle(invalidJson) shouldBe true
        shouldThrow<DccTicketingInvalidQrCodeException> {
            extractor.extract(invalidJson)
        }.errorCode shouldBe INIT_DATA_PARSE_ERR
    }
}

@Suppress("MaxLineLength")
private const val validQrCode = """{
   "protocol": "DCCVALIDATION",
   "protocolVersion": "1.0.0",
   "serviceIdentity": "https://dgca-booking-demo-eu-test.cfapps.eu10.hana.ondemand.com/api/identity",
   "privacyUrl": "https://validation-decorator.example",
   "token": "eyJ0eXAiOiJKV1QiLCJraWQiOiJiUzhEMi9XejV0WT0iLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJodHRwczovL2RnY2EtYm9va2luZy1kZW1vLWV1LXRlc3QuY2ZhcHBzLmV1MTAuaGFuYS5vbmRlbWFuZC5jb20vYXBpL2lkZW50aXR5IiwiZXhwIjoxNjM1NDk2MzYwLCJzdWIiOiIwMDI0MWQxMS0yN2I0LTQxYWYtOWU3Ny0zNDE4YzNlY2NmZDQifQ.X0wUdET3omy3qXyOhBh1UuAUEvfYMCdapv0yVShynfZpc4yS3kH57TrPLgSqS7A9ZhbgIdCIfZwr0Chm1ELyTw",
   "consent": "Please confirm to start the DCC Exchange flow. If you do not confirm, the flow is aborted.",
   "subject": "00241d11-27b4-41af-9e77-3418c3eccfd4",
   "serviceProvider": "Booking Demo"
}"""

@Suppress("MaxLineLength")
private const val invalidQrCodeMissingServiceProvider = """{
   "protocol": "DCCVALIDATION",
   "protocolVersion": "1.0.0",
   "serviceIdentity": "https://dgca-booking-demo-eu-test.cfapps.eu10.hana.ondemand.com/api/identity",
   "privacyUrl": "https://validation-decorator.example",
   "token": "eyJ0eXAiOiJKV1QiLCJraWQiOiJiUzhEMi9XejV0WT0iLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJodHRwczovL2RnY2EtYm9va2luZy1kZW1vLWV1LXRlc3QuY2ZhcHBzLmV1MTAuaGFuYS5vbmRlbWFuZC5jb20vYXBpL2lkZW50aXR5IiwiZXhwIjoxNjM1NDk2MzYwLCJzdWIiOiIwMDI0MWQxMS0yN2I0LTQxYWYtOWU3Ny0zNDE4YzNlY2NmZDQifQ.X0wUdET3omy3qXyOhBh1UuAUEvfYMCdapv0yVShynfZpc4yS3kH57TrPLgSqS7A9ZhbgIdCIfZwr0Chm1ELyTw",
   "consent": "Please confirm to start the DCC Exchange flow. If you do not confirm, the flow is aborted.",
   "subject": "00241d11-27b4-41af-9e77-3418c3eccfd4",
   "serviceProvider": "   "
}"""

@Suppress("MaxLineLength")
private const val invalidQrCodeWrongProtocol = """{
   "protocol": "WRONG",
   "protocolVersion": "1.0.0",
   "serviceIdentity": "https://dgca-booking-demo-eu-test.cfapps.eu10.hana.ondemand.com/api/identity",
   "privacyUrl": "https://validation-decorator.example",
   "token": "eyJ0eXAiOiJKV1QiLCJraWQiOiJiUzhEMi9XejV0WT0iLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJodHRwczovL2RnY2EtYm9va2luZy1kZW1vLWV1LXRlc3QuY2ZhcHBzLmV1MTAuaGFuYS5vbmRlbWFuZC5jb20vYXBpL2lkZW50aXR5IiwiZXhwIjoxNjM1NDk2MzYwLCJzdWIiOiIwMDI0MWQxMS0yN2I0LTQxYWYtOWU3Ny0zNDE4YzNlY2NmZDQifQ.X0wUdET3omy3qXyOhBh1UuAUEvfYMCdapv0yVShynfZpc4yS3kH57TrPLgSqS7A9ZhbgIdCIfZwr0Chm1ELyTw",
   "consent": "Please confirm to start the DCC Exchange flow. If you do not confirm, the flow is aborted.",
   "subject": "00241d11-27b4-41af-9e77-3418c3eccfd4",
   "serviceProvider": "Booking Demo"
}"""

@Suppress("MaxLineLength")
private const val invalidQrCodeMissingSubject = """{
   "protocol": "DCCVALIDATION",
   "protocolVersion": "1.0.0",
   "serviceIdentity": "https://dgca-booking-demo-eu-test.cfapps.eu10.hana.ondemand.com/api/identity",
   "privacyUrl": "https://validation-decorator.example",
   "token": "eyJ0eXAiOiJKV1QiLCJraWQiOiJiUzhEMi9XejV0WT0iLCJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJodHRwczovL2RnY2EtYm9va2luZy1kZW1vLWV1LXRlc3QuY2ZhcHBzLmV1MTAuaGFuYS5vbmRlbWFuZC5jb20vYXBpL2lkZW50aXR5IiwiZXhwIjoxNjM1NDk2MzYwLCJzdWIiOiIwMDI0MWQxMS0yN2I0LTQxYWYtOWU3Ny0zNDE4YzNlY2NmZDQifQ.X0wUdET3omy3qXyOhBh1UuAUEvfYMCdapv0yVShynfZpc4yS3kH57TrPLgSqS7A9ZhbgIdCIfZwr0Chm1ELyTw",
   "consent": "Please confirm to start the DCC Exchange flow. If you do not confirm, the flow is aborted.",
   "serviceProvider": "Booking Demo"
}"""

private const val invalidJson = """{
   "protocol": "DCCVALIDATION",
   "cookie": "chocolate chip",
}"""
