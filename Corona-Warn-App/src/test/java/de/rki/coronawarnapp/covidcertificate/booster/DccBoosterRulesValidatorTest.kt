package de.rki.coronawarnapp.covidcertificate.booster

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.DaggerCovidCertificateTestComponent
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultCertLogicEngine
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

    @BeforeEach
    fun setUp() {
        DaggerCovidCertificateTestComponent.create().inject(this)
        MockKAnnotations.init(this)
        coEvery { dccBoosterRulesRepository.rules } returns flowOf(emptyList())
    }

    @Test
    fun `Empty BoosterRules returns null`() = runBlockingTest {
        val mock = mockk<VaccinationCertificate>()
        validator().validateBoosterRules(listOf(mock)) shouldBe null
    }

    @Test
    fun `Empty Certificates List returns null`() = runBlockingTest {
        val mockRule = mockk<DccValidationRule>()
        coEvery { dccBoosterRulesRepository.rules } returns flowOf(listOf(mockRule))

        validator().validateBoosterRules(emptyList()) shouldBe null
    }

    //////////////////////
    // Recovery
    /////////////////////
    @Test
    fun `Most recent Rec Cert based on testedPositiveOn date`() {
        val mockRec1 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.03.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-04-01T00:00:00.000Z")
        }
        val mockRec2 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.02.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-04-01T00:00:00.000Z")
        }

        val mockRec3 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-04-01T00:00:00.000Z")
        }

        val dccList = listOf(mockRec1, mockRec2, mockRec3).shuffled()
        findRecentRecoveryCertificate(dccList) shouldBe mockRec1
    }

    @Test
    fun `Most recent Rec Cert based on issued date`() {
        val mockRec1 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-02-01T00:00:00.000Z")
        }
        val mockRec2 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-03-01T00:00:00.000Z")
        }

        val mockRec3 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-01-01T00:00:00.000Z")
        }

        val dccList = listOf(mockRec1, mockRec2, mockRec3).shuffled()
        findRecentRecoveryCertificate(dccList) shouldBe mockRec2
    }

    @Test
    fun `Most recent Rec Cert singe certificate`() {
        val mockRec1 = mockk<RecoveryCertificate>().apply {
            every { testedPositiveOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-02-01T00:00:00.000Z")
        }

        val dccList = listOf(mockRec1).shuffled()
        findRecentRecoveryCertificate(dccList) shouldBe mockRec1
    }

    @Test
    fun `Most recent Rec Cert is null when list is empty`() {
        findRecentRecoveryCertificate(emptyList()) shouldBe null
    }

    /////////////////////////
    // Vaccination
    ////////////////////////
    @Test
    fun `Most recent Vac Cert based on vaccination date`() {
        val mockVac1 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.03.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-04-01T00:00:00.000Z")
        }
        val mockVac2 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.02.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-04-01T00:00:00.000Z")
        }

        val mockVac3 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-04-01T00:00:00.000Z")
        }

        val dccList = listOf(mockVac1, mockVac2, mockVac3).shuffled()
        findRecentVaccinationCertificate(dccList) shouldBe mockVac1
    }

    @Test
    fun `Most recent Vac Cert based on issued date`() {
        val mockVac1 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-02-01T00:00:00.000Z")
        }
        val mockVac2 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-03-01T00:00:00.000Z")
        }

        val mockVac3 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-01-01T00:00:00.000Z")
        }

        val dccList = listOf(mockVac1, mockVac2, mockVac3).shuffled()
        findRecentVaccinationCertificate(dccList) shouldBe mockVac2
    }

    @Test
    fun `Most recent Vac Cert singe certificate`() {
        val mockVac1 = mockk<VaccinationCertificate>().apply {
            every { vaccinatedOn } returns LocalDate.parse("2021.01.01", dateTime)
            every { headerIssuedAt } returns Instant.parse("2020-02-01T00:00:00.000Z")
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
