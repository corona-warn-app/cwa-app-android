package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

@Suppress("MaxLineLength")
class CertLogicEngineWrapperTest : BaseTest() {

    @Inject lateinit var wrapper: CertLogicEngineWrapper
    @Inject lateinit var extractor: DccQrCodeExtractor
    @Inject lateinit var dccJsonSchema: DccJsonSchema

    @BeforeEach
    fun setup() {
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
    }

    @Test
    fun `valid certificate passes`() {
        val rule = createVaccinationRule(
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
        )
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val evaluatedRules = wrapper.process(
            rules = listOf(rule),
            validationClock = Instant.parse("2021-06-30T09:25:00.000Z"),
            certificate = certificate.data,
            countryCode = "DE",
            schemaJson = dccJsonSchema.rawSchema
        )
        evaluatedRules.size shouldBe 1
        evaluatedRules.forEach {
            it.result shouldBe DccValidationRule.Result.PASSED
        }
    }
}
