package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import dgca.verifier.app.engine.data.CertificateType
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.io.FileReader
import java.nio.file.Paths
import javax.inject.Inject

@Suppress("MaxLineLength")
class CertLogicEngineWrapperTest : BaseTest() {

    @MockK lateinit var valueSetWrapper: ValueSetWrapper

    lateinit var wrapper: CertLogicEngineWrapper
    @Inject lateinit var extractor: DccQrCodeExtractor
    @Inject lateinit var dccJsonSchema: DccJsonSchema
    @Inject @BaseGson lateinit var gson: Gson

    private val vaccinationValueMap = mapOf(countryCodeMap)

    // Json file (located in /test/resources/dcc-validation-rules-common-test-cases.json)
    private val fileName = "dcc-validation-rules-common-test-cases.json"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)
        wrapper = CertLogicEngineWrapper(valueSetWrapper)
        coEvery { valueSetWrapper.valueSetVaccination } returns flowOf(vaccinationValueMap)
        coEvery { valueSetWrapper.valueSetTest } returns flowOf(vaccinationValueMap)
        coEvery { valueSetWrapper.valueSetRecovery } returns flowOf(vaccinationValueMap)
    }

    @Test
    fun `valid certificate passes`() = runBlockingTest {
        val rule = createDccRule(
            certificateType = CertificateType.VACCINATION,
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
        )
        val ruleGeneral = createDccRule(
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

    @Test
    fun `run test cases`() = runBlocking {
        // Load and parse test data
        val jsonFile = Paths.get("src", "test", "resources", fileName).toFile()
        jsonFile shouldNotBe null
        val jsonString = FileReader(jsonFile).readText()
        jsonString.length shouldBeGreaterThan 0
        val json = gson.fromJson<CertLogicTestCases>(jsonString)
        json shouldNotBe null

        json.testCases.forEachIndexed { index, certLogicTestCase ->
            Timber.i("Test case $index: ${certLogicTestCase.description}")
            val certificate = extractor.extract(certLogicTestCase.dcc)
            val evaluatedRules = wrapper.process(
                rules = certLogicTestCase.rules,
                validationClock = Instant.ofEpochSecond(Integer.parseInt(certLogicTestCase.validationClock).toLong()),
                certificate = certificate.data,
                countryCode = certLogicTestCase.countryOfArrival,
                schemaJson = dccJsonSchema.rawSchema
            )
            evaluatedRules.size shouldBe certLogicTestCase.rules.size
            val noFailed = evaluatedRules.count { it.result == DccValidationRule.Result.FAILED }
            val noPassed = evaluatedRules.count { it.result == DccValidationRule.Result.PASSED }
            val noOpen = evaluatedRules.count { it.result == DccValidationRule.Result.OPEN }
            Timber.i("$noFailed failed, expected ${certLogicTestCase.expFail}.")
            Timber.i("$noPassed passed, expected ${certLogicTestCase.expPass}.")
            Timber.i("$noOpen open, expected ${certLogicTestCase.expOpen}.")
//            noFailed shouldBe certLogicTestCase.expFail
//            noPassed shouldBe certLogicTestCase.expPass
//            noOpen shouldBe certLogicTestCase.expOpen
        }
    }
}
