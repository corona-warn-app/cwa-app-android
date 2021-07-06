package de.rki.coronawarnapp.covidcertificate.validation.core.validation.wrapper

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class CertLogicEngineWrapperTest : BaseTest() {

    @Inject lateinit var wrapper: CertLogicEngineWrapper
    @Inject lateinit var extractor: DccQrCodeExtractor

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @Test
    fun `process valid certificate`() {
        val logic = ObjectMapper().readTree(
            """{
                "and": [
                {">=":[ {"var":"dn"}, 0 ]}
                ]
            }"""
        )
        val rule = DccValidationRule(
            identifier = "VR-DE-1",
            version = "1.0.0",
            schemaVersion = "1.0.0",
            engine = "CERTLOGIC",
            engineVersion = "1.0.0",
            typeDcc = DccValidationRule.Type.ACCEPTANCE,
            country = "DE",
            certificateType = "Vaccination",
            description = mapOf("en" to "Doses must be >= 2"),
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
            affectedFields = listOf("dn"),
            logic = logic
        )
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val evaluatedRules = wrapper.process(
            rules = listOf(rule),
            validationClock = Instant.parse("2021-06-30T09:25:00.000Z"),
            certificate = certificate.data,
            countryCode = "DE"
        )
        evaluatedRules.forEach {
            it.result shouldBe DccValidationRule.Result.PASSED
        }
    }
}
