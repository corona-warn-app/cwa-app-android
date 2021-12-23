package de.rki.coronawarnapp.covidcertificate.booster

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccHeader
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asExternalRule
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.Result
import dgca.verifier.app.engine.ValidationResult
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import dgca.verifier.app.engine.data.Rule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import javax.inject.Inject

class DccBoosterRulesValidatorTest : BaseTest() {

    @Inject lateinit var engine: Lazy<DefaultCertLogicEngine>
    @Inject @BaseJackson lateinit var objectMapper: ObjectMapper
    @MockK lateinit var dccBoosterRulesRepository: BoosterRulesRepository

    private val vacCertJson = """
        {
          "ver" : "1.2.1",
          "nam" : {
            "fn" : "Musterfrau-Gößinger",
            "gn" : "Gabriele",
            "fnt" : "MUSTERFRAU<GOESSINGER",
            "gnt" : "GABRIELE"
          },
          "dob" : "1998-02-26",
          "v" : [ {
            "tg" : "840539006",
            "vp" : "1119349007",
            "mp" : "EU/1/20/1528",
            "ma" : "ORG-100030215",
            "dn" : 1,
            "sd" : 2,
            "dt" : "2021-02-18",
            "co" : "AT",
            "is" : "Ministry of Health, Austria",
            "ci" : "URN:UVCI:01:AT:10807843F94AEE0EE5093FBC254BD813#B"
          } ]
        }
    """.trimIndent()

    private val recCertJson = """
        {
          "ver" : "1.2.1",
          "nam" : {
            "fn" : "Musterfrau-Gößinger",
            "gn" : "Gabriele",
            "fnt" : "MUSTERFRAU<GOESSINGER",
            "gnt" : "GABRIELE"
          },
          "dob" : "1998-02-26",
          "r" : [ {
            "tg" : "840539006",
            "fr" : "2021-02-20",
            "co" : "AT",
            "is" : "Ministry of Health, Austria",
            "df" : "2021-04-04",
            "du" : "2021-10-04",
            "ci" : "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
          } ]
        }
    """.trimIndent()

    private val recCertJsonMalformed1 = """
        {
          "ver" : "1.2.1",
          "nam" : {
            "fn" : "Musterfrau-Gößinger",
            "gn" : "Gabriele",
            "fnt" : "MUSTERFRAU<GOESSINGER",
            "gnt" : "GABRIELE"
          },
          "dob" : "1998-02-26",
        }
    """.trimIndent()

    private val recCertJsonMalformed2 = """
        {
          "ver" : "1.2.1",
          "nam" : {
            "fn" : "Musterfrau-Gößinger",
            "gn" : "Gabriele",
            "fnt" : "MUSTERFRAU<GOESSINGER",
            "gnt" : "GABRIELE"
          },
          "dob" : "1998-02-26",
          "r" : []
        }
    """.trimIndent()

    private val recCertJsonMalformed3 = """
        {
          "ver" : "1.2.1",
          "nam" : {
            "fn" : "Musterfrau-Gößinger",
            "gn" : "Gabriele",
            "fnt" : "MUSTERFRAU<GOESSINGER",
            "gnt" : "GABRIELE"
          },
          "dob" : "1998-02-26",
          "r" : null
        }
    """.trimIndent()

    private val recCertJsonMalformed4 = """
        {
          "ver" : "1.2.1",
          "nam" : {
            "fn" : "Musterfrau-Gößinger",
            "gn" : "Gabriele",
            "fnt" : "MUSTERFRAU<GOESSINGER",
            "gnt" : "GABRIELE"
          },
          "dob" : "1998-02-26",
          "r" : ""
        }
    """.trimIndent()

    private val payloadJson = """
        {
          "ver" : "1.2.1",
          "nam" : {
            "fn" : "Musterfrau-Gößinger",
            "gn" : "Gabriele",
            "fnt" : "MUSTERFRAU<GOESSINGER",
            "gnt" : "GABRIELE"
          },
          "dob" : "1998-02-26",
          "v" : [ {
            "tg" : "840539006",
            "vp" : "1119349007",
            "mp" : "EU/1/20/1528",
            "ma" : "ORG-100030215",
            "dn" : 1,
            "sd" : 2,
            "dt" : "2021-02-18",
            "co" : "AT",
            "is" : "Ministry of Health, Austria",
            "ci" : "URN:UVCI:01:AT:10807843F94AEE0EE5093FBC254BD813#B"
          } ],
          "r" : [ {
            "tg" : "840539006",
            "fr" : "2021-02-20",
            "co" : "AT",
            "is" : "Ministry of Health, Austria",
            "df" : "2021-04-04",
            "du" : "2021-10-04",
            "ci" : "URN:UVCI:01:AT:858CC18CFCF5965EF82F60E493349AA5#K"
          } ]
        }
    """.trimIndent()

    private val rule = DccValidationRule(
        identifier = "identifier",
        typeDcc = DccValidationRule.Type.ACCEPTANCE,
        country = DccCountry.DE,
        version = "1.0.0",
        schemaVersion = "1.3.0",
        engine = "CERTLOGIC",
        engineVersion = "0.9.0",
        certificateType = "Vaccination",
        description = emptyList(),
        validFrom = "2021-05-27T07:46:40Z",
        validTo = "2023-05-27T07:46:40Z",
        affectedFields = emptyList(),
        logic = mockk()
    )

    @BeforeEach
    fun setUp() {
        DaggerCovidCertificateTestComponent.create().inject(this)
        MockKAnnotations.init(this)
        coEvery { dccBoosterRulesRepository.updateBoosterNotificationRules() } returns emptyList()
    }

    @Test
    fun `Empty BoosterRules returns null`() = runBlockingTest {
        val mock = mockk<VaccinationCertificate>()
        validator().validateBoosterRules(listOf(mock)) shouldBe null
    }

    @Test
    fun `Empty Certificates List returns null`() = runBlockingTest {
        val mockRule = mockk<DccValidationRule>()
        coEvery { dccBoosterRulesRepository.updateBoosterNotificationRules() } returns listOf(mockRule)

        validator().validateBoosterRules(emptyList()) shouldBe null
    }

    @Test
    fun `Validate params to CertLogic - Success`() = runBlockingTest {
        val mockHeader = mockk<DccHeader>().apply {
            every { issuedAt } returns Instant.EPOCH
            every { expiresAt } returns Instant.EPOCH
        }
        val vacDccData = mockk<DccData<VaccinationDccV1>>().apply {
            every { certificateJson } returns vacCertJson
            every { header } returns mockHeader
            every { certificate } returns mockk<VaccinationDccV1>().apply {
                every { version } returns "1.2.1"
            }
        }

        val recDccData = mockk<DccData<RecoveryDccV1>>().apply {
            every { certificateJson } returns recCertJson
        }

        val mockRec1 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.03.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
            every { dccData } returns recDccData
            every { uniqueCertificateIdentifier } returns "1"
        }

        val mockVac2 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.02.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
            every { dccData } returns vacDccData
            every { uniqueCertificateIdentifier } returns "2"
        }

        coEvery { dccBoosterRulesRepository.rules } returns flowOf(listOf(rule))
        val mockEngine = mockk<DefaultCertLogicEngine>().apply {
            every { validate(any(), any(), any(), any(), any()) } answers {
                arg<CertificateType>(0) shouldBe CertificateType.VACCINATION
                arg<String>(1) shouldBe "1.2.1"
                arg<List<Rule>>(2) shouldBe listOf(rule.asExternalRule)
                arg<ExternalParameter>(3).apply {
                    countryCode shouldBe DccCountry.DE
                    issuerCountryCode shouldBe DccCountry.DE
                    valueSets shouldBe emptyMap()
                    kid shouldBe ""
                    region shouldBe ""
                    iat shouldBe "1970-01-01T00:00Z"
                    exp shouldBe "1970-01-01T00:00Z"
                }
                arg<String>(4).apply { objectMapper.readTree(this).toPrettyString() shouldBe payloadJson }

                listOf(
                    ValidationResult(
                        rule = rule.asExternalRule,
                        result = Result.PASSED,
                        current = "",
                        validationErrors = null
                    )
                )
            }
        }
        val validator = DccBoosterRulesValidator(
            boosterRulesRepository = dccBoosterRulesRepository,
            engine = { mockEngine },
            objectMapper = objectMapper
        )

        validator.validateBoosterRules(listOf(mockRec1, mockVac2))?.apply {
            rule.identifier shouldBe rule.identifier
        }
    }

    @Test
    fun `Validate params to CertLogic - Failure`() = runBlockingTest {
        val mockHeader = mockk<DccHeader>().apply {
            every { issuedAt } returns Instant.EPOCH
            every { expiresAt } returns Instant.EPOCH
        }
        val vacDccData = mockk<DccData<VaccinationDccV1>>().apply {
            every { certificateJson } returns vacCertJson
            every { header } returns mockHeader
            every { certificate } returns mockk<VaccinationDccV1>().apply {
                every { version } returns "1.2.1"
            }
        }

        val mockVac2 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.02.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
            every { dccData } returns vacDccData
            every { uniqueCertificateIdentifier } returns "2"
        }

        coEvery { dccBoosterRulesRepository.updateBoosterNotificationRules() } returns listOf(rule)
        val mockEngine = mockk<DefaultCertLogicEngine>().apply {
            every { validate(any(), any(), any(), any(), any()) } returns listOf(
                ValidationResult(
                    rule = rule.asExternalRule,
                    result = Result.FAIL,
                    current = "",
                    validationErrors = listOf(Exception())
                )
            )
        }
        val validator = DccBoosterRulesValidator(
            boosterRulesRepository = dccBoosterRulesRepository,
            engine = { mockEngine },
            objectMapper = objectMapper
        )

        validator.validateBoosterRules(listOf(mockVac2)) shouldBe null
    }

    @Test
    fun `Validate params to CertLogic - no rules returned`() = runBlockingTest {
        val mockHeader = mockk<DccHeader>().apply {
            every { issuedAt } returns Instant.EPOCH
            every { expiresAt } returns Instant.EPOCH
        }
        val vacDccData = mockk<DccData<VaccinationDccV1>>().apply {
            every { certificateJson } returns vacCertJson
            every { header } returns mockHeader
            every { certificate } returns mockk<VaccinationDccV1>().apply {
                every { version } returns "1.2.1"
            }
        }

        val mockVac2 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.02.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
            every { dccData } returns vacDccData
            every { uniqueCertificateIdentifier } returns "2"
        }

        coEvery { dccBoosterRulesRepository.updateBoosterNotificationRules() } returns listOf(rule)
        val mockEngine = mockk<DefaultCertLogicEngine>().apply {
            every { validate(any(), any(), any(), any(), any()) } returns listOf()
        }
        val validator = DccBoosterRulesValidator(
            boosterRulesRepository = dccBoosterRulesRepository,
            engine = { mockEngine },
            objectMapper = objectMapper
        )

        validator.validateBoosterRules(listOf(mockVac2)) shouldBe null
    }

    @Test
    fun `Constructed payload should be Vac Json in case of failure`() {
        val vacDccData = mockk<DccData<VaccinationDccV1>>().apply {
            every { certificateJson } returns vacCertJson
        }

        val recDccData = mockk<DccData<VaccinationDccV1>>().apply {
            every { certificateJson } returns recCertJson
        }

        val objectMapper2 = mockk<ObjectMapper>().apply {
            every { readTree(any<String>()) } throws Exception("Crash \uD83D\uDCA5")
        }

        val validator = DccBoosterRulesValidator(
            boosterRulesRepository = dccBoosterRulesRepository,
            engine = engine,
            objectMapper = objectMapper2
        )
        val payload = validator.payload(vacDccData, recDccData)
        objectMapper.readTree(payload).toPrettyString() shouldBe vacCertJson
    }

    @Test
    fun `Constructed payload should be Vac Json`() {
        val vacDccData = mockk<DccData<VaccinationDccV1>>().apply {
            every { certificateJson } returns vacCertJson
        }

        val payload = validator().payload(vacDccData, null)
        objectMapper.readTree(payload).toPrettyString() shouldBe vacCertJson
    }

    @Test
    fun `Constructed payload should have r0`() {
        val vacDccData = mockk<DccData<VaccinationDccV1>>().apply {
            every { certificateJson } returns vacCertJson
        }

        val recDccData = mockk<DccData<RecoveryDccV1>>().apply {
            every { certificateJson } returns recCertJson
        }
        val payload = validator().payload(vacDccData, recDccData)
        objectMapper.readTree(payload).toPrettyString() shouldBe payloadJson
    }

    @Test
    fun `Constructed payload should be Vac Json when r0 is missing for Rec Certficate`() {
        listOf(
            recCertJsonMalformed1,
            recCertJsonMalformed2,
            recCertJsonMalformed3,
            recCertJsonMalformed4
        ).forEach { json ->
            val vacDccData = mockk<DccData<VaccinationDccV1>>().apply {
                every { certificateJson } returns vacCertJson
            }

            val recDccData = mockk<DccData<RecoveryDccV1>>().apply {
                every { certificateJson } returns json
            }
            val payload = validator().payload(vacDccData, recDccData)
            objectMapper.readTree(payload).toPrettyString() shouldBe vacCertJson
        }
    }

    // ////////////////////
    // Recovery
    // ///////////////////
    @Test
    fun `Most recent Rec Cert based on testedPositiveOn date`() {
        val mockRec1 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.03.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
        }
        val mockRec2 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.02.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
        }

        val mockRec3 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
        }

        val dccList = listOf(mockRec1, mockRec2, mockRec3).shuffled()
        findRecentRecoveryCertificate(dccList) shouldBe mockRec1
    }

    @Test
    fun `Most recent Rec Cert based on issuedAt date`() {
        val mockRec1 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-02-01T00:00:00.000Z")
        }
        val mockRec2 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-03-01T00:00:00.000Z")
        }

        val mockRec3 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-01-01T00:00:00.000Z")
        }

        val dccList = listOf(mockRec1, mockRec2, mockRec3).shuffled()
        findRecentRecoveryCertificate(dccList) shouldBe mockRec2
    }

    @Test
    fun `Most recent Rec Cert singe certificate`() {
        val mockRec1 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-02-01T00:00:00.000Z")
        }

        val dccList = listOf(mockRec1).shuffled()
        findRecentRecoveryCertificate(dccList) shouldBe mockRec1
    }

    @Test
    fun `Most recent Rec Cert is null when list is empty`() {
        findRecentRecoveryCertificate(emptyList()) shouldBe null
    }

    // ///////////////////////
    // Vaccination
    // //////////////////////
    @Test
    fun `Most recent Vac Cert based on vaccinationOn date`() {
        val mockVac1 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.03.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
        }
        val mockVac2 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.02.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
        }

        val mockVac3 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-04-01T00:00:00.000Z")
        }

        val dccList = listOf(mockVac1, mockVac2, mockVac3).shuffled()
        findRecentVaccinationCertificate(dccList) shouldBe mockVac1
    }

    @Test
    fun `Most recent Vac Cert based on issuedAt date`() {
        val mockVac1 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-02-01T00:00:00.000Z")
        }
        val mockVac2 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-03-01T00:00:00.000Z")
        }

        val mockVac3 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-01-01T00:00:00.000Z")
        }

        val dccList = listOf(mockVac1, mockVac2, mockVac3).shuffled()
        findRecentVaccinationCertificate(dccList) shouldBe mockVac2
    }

    @Test
    fun `Most recent Vac Cert singe certificate`() {
        val mockVac1 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2021-02-01T00:00:00.000Z")
        }

        val dccList = listOf(mockVac1).shuffled()
        findRecentVaccinationCertificate(dccList) shouldBe mockVac1
    }

    @Test
    fun `Most recent Vac Cert is null when list is empty`() {
        findRecentVaccinationCertificate(emptyList()) shouldBe null
    }

    private fun validator() = DccBoosterRulesValidator(
        boosterRulesRepository = dccBoosterRulesRepository,
        engine = engine,
        objectMapper = objectMapper
    )

    companion object {
        private val dateTime = DateTimeFormat.forPattern("yyyy.MM.dd")
    }
}
