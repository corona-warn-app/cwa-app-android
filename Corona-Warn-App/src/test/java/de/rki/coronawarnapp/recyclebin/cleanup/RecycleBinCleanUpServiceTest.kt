package de.rki.coronawarnapp.recyclebin.cleanup

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.reyclebin.RecycledItemsProvider
import de.rki.coronawarnapp.reyclebin.cleanup.RecycleBinCleanUpService
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Days
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RecycleBinCleanUpServiceTest : BaseTest() {

    private val now = Instant.parse("2021-10-13T12:00:00.000Z")

    @MockK lateinit var timeStamper: TimeStamper
    @RelaxedMockK lateinit var recycledItemsProvider: RecycledItemsProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.parse("2021-10-13T12:00:00.000Z")
    }

    private fun createInstance() = RecycleBinCleanUpService(
        recycledItemsProvider = recycledItemsProvider,
        timeStamper = timeStamper
    )

    private fun createCert(days: Int): CwaCovidCertificate {
        val mockContainerId = mockk<CertificateContainerId>()
        val recycleTime = now.minus(Days.days(days).toStandardDuration())
        return mockk {
            every { recycledAt } returns recycleTime
            every { containerId } returns mockContainerId
        }
    }

    @Test
    fun `Check days of retention for recycle bin`() {
        RecycleBinCleanUpService.RETENTION_DAYS.standardDays shouldBe 30
    }

    @Test
    fun `No recycled items, nothing to delete`() = runBlockingTest {
        every { recycledItemsProvider.recycledCertificates } returns flowOf(emptySet())

        createInstance().clearRecycledCertificates()

        coVerify(exactly = 0) { recycledItemsProvider.deleteAllCertificate(any()) }
    }

    @Test
    fun `Retention time in recycle bin too short, nothing to delete`() = runBlockingTest {
        val certWith5DaysOfRetention = createCert(5)
        val certWith15DaysOfRetention = createCert(15)
        val certWith25DaysOfRetention = createCert(25)

        every { recycledItemsProvider.recycledCertificates } returns flowOf(
            setOf(certWith5DaysOfRetention, certWith15DaysOfRetention, certWith25DaysOfRetention)
        )

        createInstance().clearRecycledCertificates()

        coVerify(exactly = 0) { recycledItemsProvider.deleteAllCertificate(any()) }
    }

    @Test
    fun `Only deletes certs with more than 30 days of retention in recycle bin`() = runBlockingTest {
        val certWith29DaysOfRetention = createCert(29)
        val certWith30DaysOfRetention = createCert(30)
        val certWith31DaysOfRetention = createCert(31)
        val certWith32DaysOfRetention = createCert(32)

        every { recycledItemsProvider.recycledCertificates } returns flowOf(
            setOf(certWith29DaysOfRetention, certWith30DaysOfRetention, certWith31DaysOfRetention, certWith32DaysOfRetention)
        )

        createInstance().clearRecycledCertificates()

        val containerIds = listOf(certWith31DaysOfRetention.containerId, certWith32DaysOfRetention.containerId)
        coVerify(exactly = 1) { recycledItemsProvider.deleteAllCertificate(containerIds) }
    }
}
