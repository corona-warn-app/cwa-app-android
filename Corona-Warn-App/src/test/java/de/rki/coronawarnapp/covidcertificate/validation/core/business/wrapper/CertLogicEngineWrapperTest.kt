package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import dgca.verifier.app.engine.data.CertificateType
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

@Suppress("MaxLineLength")
class CertLogicEngineWrapperTest : BaseTest() {

    @MockK lateinit var valueSetWrapper: ValueSetWrapper

    lateinit var wrapper: CertLogicEngineWrapper
    @Inject lateinit var extractor: DccQrCodeExtractor
    @Inject lateinit var dccJsonSchema: DccJsonSchema

    private val vaccinationValueMap = mapOf(countryCodeMap)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        wrapper = CertLogicEngineWrapper(valueSetWrapper)
        coEvery { valueSetWrapper.valueSetVaccination } returns flowOf(vaccinationValueMap)
    }

    @Test
    fun `valid certificate passes`() = runBlockingTest {
        val rule = createRule(
            certificateType = CertificateType.VACCINATION,
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
        )
        val ruleGeneral = createRule(
            certificateType = CertificateType.GENERAL,
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
        )
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val evaluatedRules = wrapper.process(
            rules = listOf(rule, ruleGeneral),
            validationClock = Instant.parse("2021-06-30T09:25:00.000Z"),
            certificate = certificate.data,
            countryCode = "DE",
            schemaJson = dccJsonSchema.rawSchema
        )
        evaluatedRules.size shouldBe 2
        evaluatedRules.forEach {
            it.result shouldBe DccValidationRule.Result.PASSED
        }
    }
}
