package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.google.gson.Gson
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.internal.toValueSetsContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyValueSetsContainer
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import dgca.verifier.app.engine.data.CertificateType
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeBase64
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.io.FileReader
import java.nio.file.Paths
import java.util.Locale
import javax.inject.Inject

@Suppress("MaxLineLength")
class CertLogicEngineWrapperTest : BaseTest() {

    lateinit var valueSetWrapper: ValueSetWrapper
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var dccValidationRepository: DccValidationRepository

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

        coEvery { dccValidationRepository.dccCountries } returns flowOf(countryCodes.map { DccCountry(it) })
        coEvery { valueSetsRepository.latestVaccinationValueSets } returns flowOf(emptyValueSetsContainer.vaccinationValueSets)
        coEvery { valueSetsRepository.latestTestCertificateValueSets } returns flowOf(emptyValueSetsContainer.testCertificateValueSets)
    }

    @Test
    fun `valid certificate passes`() = runBlockingTest {
        valueSetWrapper = ValueSetWrapper(valueSetsRepository, dccValidationRepository)
        wrapper = CertLogicEngineWrapper(valueSetWrapper)
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

        val valueSets =
            ValueSetsOuterClass.ValueSets.parseFrom(json.general.valueSetProtocolBuffer.decodeBase64()!!.toByteArray())
        val container = valueSets.toValueSetsContainer(languageCode = Locale.GERMAN)
        coEvery { valueSetsRepository.latestVaccinationValueSets } returns flowOf(container.vaccinationValueSets)
        coEvery { valueSetsRepository.latestTestCertificateValueSets } returns flowOf(container.testCertificateValueSets)
        valueSetWrapper = ValueSetWrapper(valueSetsRepository, dccValidationRepository)
        wrapper = CertLogicEngineWrapper(valueSetWrapper)

        json.testCases.forEachIndexed { index, certLogicTestCase ->
            val certificate = extractor.extract(certLogicTestCase.dcc)
            val validationClock = Instant.ofEpochSecond(Integer.parseInt(certLogicTestCase.validationClock).toLong())

            val acceptanceRules = certLogicTestCase.rules.filter {
                it.typeDcc == DccValidationRule.Type.ACCEPTANCE
            }.filterRelevantRules(
                validationClock = validationClock,
                country = DccCountry(certLogicTestCase.countryOfArrival),
                certificateType = certificate.data.type
            )
            val evaluatedAcceptanceRules = wrapper.process(
                rules = acceptanceRules,
                validationClock = validationClock,
                certificate = certificate.data,
                countryCode = certLogicTestCase.countryOfArrival,
                schemaJson = dccJsonSchema.rawSchema
            )

            val invalidationRules = certLogicTestCase.rules.filter {
                it.typeDcc == DccValidationRule.Type.INVALIDATION
            }.filterRelevantRules(
                validationClock = validationClock,
                country = DccCountry(certificate.data.header.issuer),
                certificateType = certificate.data.type
            )

            val evaluatedInvalidationRules = wrapper.process(
                rules = invalidationRules,
                validationClock = validationClock,
                certificate = certificate.data,
                countryCode = certificate.data.header.issuer,
                schemaJson = dccJsonSchema.rawSchema
            )

            evaluatedAcceptanceRules.size shouldBe acceptanceRules.size
            evaluatedInvalidationRules.size shouldBe invalidationRules.size
            val noFailed = evaluatedAcceptanceRules.count { it.result == DccValidationRule.Result.FAILED } +
                evaluatedInvalidationRules.count { it.result == DccValidationRule.Result.FAILED }
            val noPassed = evaluatedAcceptanceRules.count { it.result == DccValidationRule.Result.PASSED } +
                evaluatedInvalidationRules.count { it.result == DccValidationRule.Result.PASSED }
            val noOpen = evaluatedAcceptanceRules.count { it.result == DccValidationRule.Result.OPEN } +
                evaluatedInvalidationRules.count { it.result == DccValidationRule.Result.OPEN }
            Timber.i("Result for test case $index: ${certLogicTestCase.description}")
            Timber.i("$noFailed failed, expected ${certLogicTestCase.expFail}.")
            Timber.i("$noPassed passed, expected ${certLogicTestCase.expPass}.")
            Timber.i("$noOpen open, expected ${certLogicTestCase.expOpen}.")
            noFailed shouldBe certLogicTestCase.expFail
            noPassed shouldBe certLogicTestCase.expPass
            noOpen shouldBe certLogicTestCase.expOpen
        }
    }
}
