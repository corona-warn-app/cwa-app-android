package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.google.gson.Gson
import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.test.TestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationQrCodeTestData
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import de.rki.coronawarnapp.covidcertificate.valueset.internal.toValueSetsContainer
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.DefaultValueSet
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.VaccinationValueSets
import de.rki.coronawarnapp.covidcertificate.valueset.valuesets.emptyValueSetsContainer
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValueSetsOuterClass
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.data.RuleCertificateType
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.io.FileReader
import java.nio.file.Paths
import java.util.Locale
import javax.inject.Inject

class CertLogicEngineWrapperTest : BaseTest() {

    lateinit var valueSetWrapper: ValueSetWrapper
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var dccValidationRepository: DccValidationRepository

    lateinit var wrapper: CertLogicEngineWrapper

    @Inject lateinit var extractor: DccQrCodeExtractor
    @Inject lateinit var engine: Lazy<DefaultCertLogicEngine>
    @Inject @BaseGson lateinit var gson: Gson

    // Json file (located in /test/resources/dcc-validation-rules-common-test-cases.json)
    private val fileName = "dcc-validation-rules-common-test-cases.json"

    private val timeZoneOffsetBerlin = 2 // Berlin

    private val valueSet = VaccinationValueSets(
        languageCode = Locale.ENGLISH,
        tg = DefaultValueSet(),
        vp = DefaultValueSet(),
        mp = DefaultValueSet(),
        ma = DefaultValueSet(listOf(DefaultValueSet.DefaultItem(key = "ORG-100031184", "ORG-100031184")))
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        DaggerCovidCertificateTestComponent.factory().create().inject(this)

        coEvery { dccValidationRepository.dccCountries } returns flowOf(countryCodes.map { DccCountry(it) })
        coEvery { valueSetsRepository.latestVaccinationValueSets } returns
            flowOf(valueSet)
        coEvery { valueSetsRepository.latestTestCertificateValueSets } returns
            flowOf(emptyValueSetsContainer.testCertificateValueSets)
    }

    @Test
    fun `valid certificate passes`() = runTest {
        createWrapperInstance()
        val rule = createDccRule(
            certificateType = RuleCertificateType.VACCINATION,
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
        )
        val ruleGeneral = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
        )
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDateTime = DateTime.parse("2022-06-11T14:23:00+02:00")
        val evaluatedRules = wrapper.process(
            rules = listOf(rule, ruleGeneral),
            validationDateTime = validationDateTime,
            certificate = certificate.data,
            countryCode = "DE",
        )
        evaluatedRules.size shouldBe 2
        evaluatedRules.forEach {
            it.result shouldBe DccValidationRule.Result.PASSED
        }
    }

    @Test
    fun `valid certificate passes french rule`() = runTest {
        createWrapperInstance()
        val rule = createDccRule(
            certificateType = RuleCertificateType.VACCINATION,
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
            logic = frenchRule
        )
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(VaccinationQrCodeTestData.passGermanReferenceCase)
        val validationDateTime = DateTime.parse("2021-11-11T14:23:00+02:00")
        val evaluatedRules = wrapper.process(
            rules = listOf(rule),
            validationDateTime = validationDateTime,
            certificate = certificate.data,
            countryCode = "DE",
        )
        evaluatedRules.size shouldBe 1
        evaluatedRules.forEach {
            it.result shouldBe DccValidationRule.Result.PASSED
        }
    }

    @Test
    fun `PCR test certificate only valid for 72h`() = runTest {
        createWrapperInstance()
        val rule = createDccRule(
            certificateType = RuleCertificateType.TEST,
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
        )
        val ruleGeneral = createDccRule(
            certificateType = RuleCertificateType.GENERAL,
            validFrom = "2021-05-27T07:46:40Z",
            validTo = "2022-08-01T07:46:40Z",
        )
        // certificate valid until 2022-06-11T14:23:17.000Z
        val certificate = extractor.extract(TestData.qrCodePcrTest)
        val validationDateTime = LocalDateTime.parse("2021-07-20T19:10:00") // should be valid
        val evaluatedRules = wrapper.process(
            rules = listOf(rule, ruleGeneral),
            validationDateTime = validationDateTime.toDateTime(DateTimeZone.forOffsetHours(timeZoneOffsetBerlin)),
            certificate = certificate.data,
            countryCode = "DE",
        )
        evaluatedRules.size shouldBe 2
        evaluatedRules.forEach {
            it.result shouldBe DccValidationRule.Result.PASSED
        }

        val invalidationDateTime = LocalDateTime.parse("2021-07-20T19:20:00") // should be invalid

        val evaluatedRules2 = wrapper.process(
            rules = listOf(rule, ruleGeneral),
            validationDateTime = invalidationDateTime.toDateTime(DateTimeZone.forOffsetHours(timeZoneOffsetBerlin)),
            certificate = certificate.data,
            countryCode = "DE",
        )
        evaluatedRules2.size shouldBe 2
        evaluatedRules2.count {
            it.result == DccValidationRule.Result.PASSED
        } shouldBe 1
        evaluatedRules2.count {
            it.result == DccValidationRule.Result.FAILED
        } shouldBe 1
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
        coEvery { valueSetsRepository.latestVaccinationValueSets } returns
            flowOf(container.vaccinationValueSets)
        coEvery { valueSetsRepository.latestTestCertificateValueSets } returns
            flowOf(container.testCertificateValueSets)

        createWrapperInstance()

        json.testCases.forEachIndexed { index, certLogicTestCase ->
            val certificate = extractor.extract(certLogicTestCase.dcc)
            val validationClock = Instant.ofEpochSecond(Integer.parseInt(certLogicTestCase.validationClock).toLong())
            val validationDateTime = validationClock.toDateTime()
            val acceptanceRules = certLogicTestCase.rules.filter {
                it.typeDcc == DccValidationRule.Type.ACCEPTANCE
            }.filterRelevantRules(
                validationDateTime = validationDateTime,
                country = DccCountry(certLogicTestCase.countryOfArrival),
                certificateType = certificate.data.typeString
            )
            val evaluatedAcceptanceRules = wrapper.process(
                rules = acceptanceRules,
                validationDateTime = validationDateTime,
                certificate = certificate.data,
                countryCode = certLogicTestCase.countryOfArrival,
            )

            val invalidationRules = certLogicTestCase.rules.filter {
                it.typeDcc == DccValidationRule.Type.INVALIDATION
            }.filterRelevantRules(
                validationDateTime = validationDateTime,
                country = DccCountry(certificate.data.header.issuer),
                certificateType = certificate.data.typeString
            )

            val evaluatedInvalidationRules = wrapper.process(
                rules = invalidationRules,
                validationDateTime = validationDateTime,
                certificate = certificate.data,
                countryCode = certificate.data.header.issuer,
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

    private fun createWrapperInstance() {
        valueSetWrapper = ValueSetWrapper(valueSetsRepository, dccValidationRepository)
        wrapper = CertLogicEngineWrapper(valueSetWrapper, engine)
    }
}
