package de.rki.coronawarnapp.dccticketing.core.certificateselection

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class DccTicketingCertificateFilterTest : BaseTest() {

    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var recoveryCertificateRepository: RecoveryCertificateRepository

    // ///////////////////////////////////////////////////////////
    // Vaccinations
    // Person1
    private val vc1 = mockCertificate<VaccinationCertificate, VaccinationDccV1>(
        gName = "Erik", fName = "Müller", date = "1980-12-10"
    )
    private val vc2 = mockCertificate<VaccinationCertificate, VaccinationDccV1>(
        gName = "Erik", fName = "Müller", date = "1980-12-10"
    )

    // Person2
    private val vc3 = mockCertificate<VaccinationCertificate, VaccinationDccV1>(
        gName = "Max", fName = "Mustermann", date = "1990-10-10"
    )
    private val vc4 = mockCertificate<VaccinationCertificate, VaccinationDccV1>(
        gName = "Max", fName = "Mustermann", date = "1990-10-10"
    )

    // Person3
    private val vc5 = mockCertificate<VaccinationCertificate, VaccinationDccV1>(
        gName = "Eli", fName = "Mustermann", date = "1940-07-02"
    )
    private val vc6 = mockCertificate<VaccinationCertificate, VaccinationDccV1>(
        gName = "Eli", fName = "Mustermann", date = "1940-07-02"
    )
    private val vcSet = setOf(vc1, vc2, vc3, vc4, vc5, vc6)

    // ///////////////////////////////////////////////////////////
    // Recovery
    // Person1
    private val rc1 = mockCertificate<RecoveryCertificate, RecoveryDccV1>(
        gName = "Max", fName = "Mustermann", date = "1990-10-10"
    )

    // Person2
    private val rc2 = mockCertificate<RecoveryCertificate, RecoveryDccV1>(
        gName = "Erik", fName = "Müller", date = "1980-12-10"
    )

    // Person3
    private val rc3 = mockCertificate<RecoveryCertificate, RecoveryDccV1>(
        gName = "Thomas", fName = "Müller", date = "1990-10-10"
    )

    // Person4
    private val rc4 = mockCertificate<RecoveryCertificate, RecoveryDccV1>(
        gName = "Eli", fName = "Mustermann", date = "1940-07-02"
    )
    private val rcSet = setOf(rc1, rc2, rc3, rc4)

    // ///////////////////////////////////////////////////////////
    // Test
    private val tc1 = mockCertificate<TestCertificate, TestDccV1>(
        gName = "Max", fName = "Mustermann", date = "1990-10-10"
    )
    private val tc2 = mockCertificate<TestCertificate, TestDccV1>(
        gName = "Erik", fName = "Müller", date = "1980-12-10"
    )
    private val tc3 = mockCertificate<TestCertificate, TestDccV1>(
        gName = "Thomas", fName = "Müller", date = "1990-10-10"
    )
    private val tc4 = mockCertificate<TestCertificate, TestDccV1>(
        gName = "Eli", fName = "Mustermann", date = "1940-07-02"
    )
    private val tcSet = setOf(tc1, tc2, tc3, tc4)

    private val validationCondition = DccTicketingValidationCondition(
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
        category = listOf("Standard"),
        validationClock = "2021-11-03T15:39:43+00:00",
        validFrom = "2021-11-03T07:15:43+00:00",
        validTo = "2021-11-03T15:39:43+00:00",
        hash = null
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { vaccinationRepository.cwaCertificates } returns flowOf(vcSet)
        every { recoveryCertificateRepository.cwaCertificates } returns flowOf(rcSet)
        every { testCertificateRepository.cwaCertificates } returns flowOf(tcSet)
    }

    @Test
    fun `filter - conditions type=no, gnt=yes, fnt=yes, dob=yes but not in Persons`() =
        runBlockingTest {
            instance().filter(
                validationCondition.copy(
                    type = emptyList(),
                    gnt = "HANS",
                    fnt = "WURST",
                    dob = "1990-01-01",
                )
            ) shouldBe emptySet()
        }

    @Test
    fun `filter - conditions type=no, gnt=no, fnt=no, dob=no`() =
        runBlockingTest {
            instance().filter(
                validationCondition.copy(
                    type = emptyList(),
                    gnt = null,
                    fnt = null,
                    dob = null,
                )
            ) shouldBe vcSet + rcSet + tcSet
        }

    @Test
    fun `filter - conditions type=null, gnt=no, fnt=no, dob=no`() =
        runBlockingTest {
            instance().filter(
                validationCondition.copy(
                    type = null,
                    gnt = null,
                    fnt = null,
                    dob = null,
                )
            ) shouldBe vcSet + rcSet + tcSet
        }

    @Test
    fun `filter - no conditions`() =
        runBlockingTest {
            instance().filter(
                null
            ) shouldBe vcSet + rcSet + tcSet
        }

    @Test
    fun `filter - conditions type=v, gnt=no, fnt=no, dob=no`() =
        runBlockingTest {
            instance().filter(
                validationCondition.copy(
                    type = listOf("v"),
                    gnt = null,
                    fnt = null,
                    dob = null,
                )
            ) shouldBe vcSet
        }

    @Test
    fun `filter - conditions type=r, gnt=no, fnt=no, dob=no`() =
        runBlockingTest {
            instance().filter(
                validationCondition.copy(
                    type = listOf("r"),
                    gnt = null,
                    fnt = null,
                    dob = null,
                )
            ) shouldBe rcSet
        }

    @Test
    fun `filter - conditions type=t, gnt=no, fnt=no, dob=no`() =
        runBlockingTest {
            instance().filter(
                validationCondition.copy(
                    type = listOf("t"),
                    gnt = null,
                    fnt = null,
                    dob = null,
                )
            ) shouldBe tcSet
        }

    @Test
    fun `filter - conditions Max Mustermann`() =
        runBlockingTest {
            instance().filter(
                validationCondition.copy(
                    type = listOf("t", "v", "r"),
                    gnt = "Max",
                    fnt = "Mustermann",
                    dob = "1990-10-10",
                )
            ) shouldBe setOf(tc1, rc1, vc3, vc4)
        }

    @Test
    fun `filter - Unsupported types`() =
        runBlockingTest {
            instance().filter(
                validationCondition.copy(
                    type = listOf("T", "V", "R"),
                    gnt = "Max",
                    fnt = "Mustermann",
                    dob = "1990-10-10",
                )
            ) shouldBe emptySet()
        }

    private fun instance() = DccTicketingCertificateFilter(
        vaccinationRepository = vaccinationRepository,
        testCertificateRepository = testCertificateRepository,
        recoveryCertificateRepository = recoveryCertificateRepository
    )

    private inline fun <reified T : CwaCovidCertificate, reified D : DccV1.MetaData> mockCertificate(
        fName: String,
        gName: String,
        date: String
    ) = mockk<T>().apply {
        every { rawCertificate } returns mockk<D>().apply {
            every { dob } returns date
            every { nameData } returns mockk<DccV1.NameData>()
                .apply {
                    every { familyNameStandardized } returns fName
                    every { givenNameStandardized } returns gName
                }
        }
    }
}
