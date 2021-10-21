package de.rki.coronawarnapp.recyclebin.cleanup

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
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
    @RelaxedMockK lateinit var recycledCertificatesProvider: RecycledCertificatesProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.parse("2021-10-13T12:00:00.000Z")
    }

    private fun createInstance() = RecycleBinCleanUpService(
        recycledItemsProvider = recycledCertificatesProvider,
        timeStamper = timeStamper
    )

    private fun createCert(days: Int) = createCert(recycleTime = now.minus(Days.days(days).toStandardDuration()))

    private fun createCert(recycleTime: Instant): CwaCovidCertificate {
        val mockContainerId = mockk<CertificateContainerId>()
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
        every { recycledCertificatesProvider.recycledCertificates } returns flowOf(emptySet())

        createInstance().clearRecycledCertificates()

        coVerify(exactly = 0) { recycledCertificatesProvider.deleteAllCertificate(any()) }
    }

    @Test
    fun `Retention time in recycle bin too short, nothing to delete`() = runBlockingTest {
        val certWith5DaysOfRetention = createCert(5)
        val certWith15DaysOfRetention = createCert(15)
        val certWith25DaysOfRetention = createCert(25)

        every { recycledCertificatesProvider.recycledCertificates } returns flowOf(
            setOf(certWith5DaysOfRetention, certWith15DaysOfRetention, certWith25DaysOfRetention)
        )

        createInstance().clearRecycledCertificates()

        coVerify(exactly = 0) { recycledCertificatesProvider.deleteAllCertificate(any()) }
    }

    @Test
    fun `Time difference between recycledAt and now is greater than 30 days with ms precision`() = runBlockingTest {
        val nowMinus30Days = now.minus(Days.days(30).toStandardDuration())
        val certExact30Days = createCert(nowMinus30Days)
        val cert30DaysAnd1Ms = createCert(nowMinus30Days.minus(1))

        every { recycledCertificatesProvider.recycledCertificates } returns flowOf(
            setOf(
                certExact30Days,
                cert30DaysAnd1Ms
            )
        )

        createInstance().clearRecycledCertificates()

        val containerIds = listOf(cert30DaysAnd1Ms.containerId)
        coVerify(exactly = 1) { recycledCertificatesProvider.deleteAllCertificate(containerIds) }
    }
}
