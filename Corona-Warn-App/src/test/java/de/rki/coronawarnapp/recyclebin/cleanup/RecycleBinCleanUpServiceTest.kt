package de.rki.coronawarnapp.recyclebin.cleanup

import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpService
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Duration
import java.time.Instant

class RecycleBinCleanUpServiceTest : BaseTest() {

    private val now = Instant.parse("2021-10-13T12:00:00.000Z")

    @MockK lateinit var timeStamper: TimeStamper
    @RelaxedMockK lateinit var recycledCertificatesProvider: RecycledCertificatesProvider
    @RelaxedMockK lateinit var recycledCoronaTestsProvider: RecycledCoronaTestsProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.parse("2021-10-13T12:00:00.000Z")

        coEvery { recycledCoronaTestsProvider.deleteCoronaTest(any()) } just Runs
    }

    private fun createInstance() = RecycleBinCleanUpService(
        recycledCertificatesProvider = recycledCertificatesProvider,
        recycledCoronaTestsProvider = recycledCoronaTestsProvider,
        timeStamper = timeStamper
    )

    private fun createCert(days: Long) = createCert(recycleTime = now.minus(Duration.ofDays(days)))

    private fun createCert(recycleTime: Instant): CwaCovidCertificate {
        val mockContainerId = mockk<CertificateContainerId>()
        return mockk {
            every { recycledAt } returns recycleTime
            every { containerId } returns mockContainerId
        }
    }

    private fun createTest(days: Long) = createTest(recycleTime = now.minus(Duration.ofDays(days)))
    private fun familyTest(days: Long) = createFamilyTest(recycleTime = now.minus(Duration.ofDays(days)))

    private fun createTest(recycleTime: Instant): PersonalCoronaTest = mockk {
        every { recycledAt } returns recycleTime
        every { identifier } returns recycleTime.toString()
    }

    private fun createFamilyTest(recycleTime: Instant): FamilyCoronaTest = mockk {
        every { recycledAt } returns recycleTime
        every { identifier } returns recycleTime.toString()
    }

    @Test
    fun `No recycled items, nothing to delete`() = runTest {
        every { recycledCertificatesProvider.recycledCertificates } returns flowOf(emptySet())
        every { recycledCoronaTestsProvider.tests } returns flowOf(emptySet())

        createInstance().clearRecycledItems()

        coVerify(exactly = 0) {
            recycledCertificatesProvider.deleteAllCertificate(any())
            recycledCoronaTestsProvider.deleteAllCoronaTest(any())
        }
    }

    @Test
    fun `Retention time in recycle bin too short, nothing to delete`() = runTest {
        val certWith0DaysOfRetention = createCert(0)
        val certWith30DaysOfRetention = createCert(30)
        val testWith0DaysOfRetention = createTest(0)
        val testWith30DaysOfRetention = createTest(30)
        val familyTestWith0DaysOfRetention = familyTest(0)
        val familyTestWith30DaysOfRetention = familyTest(30)

        every { recycledCertificatesProvider.recycledCertificates } returns flowOf(
            setOf(certWith0DaysOfRetention, certWith30DaysOfRetention)
        )

        every { recycledCoronaTestsProvider.tests } returns flowOf(
            setOf(
                testWith0DaysOfRetention,
                testWith30DaysOfRetention,
                familyTestWith0DaysOfRetention,
                familyTestWith30DaysOfRetention
            )
        )

        createInstance().clearRecycledItems()

        coVerify(exactly = 0) {
            recycledCertificatesProvider.deleteAllCertificate(any())
            recycledCoronaTestsProvider.deleteAllCoronaTest(any())
        }
    }

    @Test
    fun `Time difference between recycledAt and now is greater than 30 days with ms precision`() = runTest {
        val nowMinus30Days = now.minus(Duration.ofDays(30))
        val nowMinus30DaysAnd1Ms = nowMinus30Days.minusMillis(1)

        val certExact30Days = createCert(nowMinus30Days)
        val cert30DaysAnd1Ms = createCert(nowMinus30DaysAnd1Ms)
        val testExact30Days = createTest(nowMinus30Days)
        val test30DaysAnd1Ms = createTest(nowMinus30DaysAnd1Ms)

        val familyTestExact30Days = createFamilyTest(nowMinus30Days)
        val familyTest30DaysAnd1Ms = createFamilyTest(nowMinus30DaysAnd1Ms)

        every { recycledCertificatesProvider.recycledCertificates } returns flowOf(
            setOf(certExact30Days, cert30DaysAnd1Ms)
        )

        every { recycledCoronaTestsProvider.tests } returns flowOf(
            setOf(testExact30Days, test30DaysAnd1Ms, familyTest30DaysAnd1Ms, familyTestExact30Days)
        )

        createInstance().clearRecycledItems()

        val containerIds = listOf(cert30DaysAnd1Ms.containerId)
        val identifiers = listOf(test30DaysAnd1Ms.identifier, familyTest30DaysAnd1Ms.identifier)
        coVerify(exactly = 1) {
            recycledCertificatesProvider.deleteAllCertificate(containerIds)
            recycledCoronaTestsProvider.deleteAllCoronaTest(identifiers)
        }
    }
}
