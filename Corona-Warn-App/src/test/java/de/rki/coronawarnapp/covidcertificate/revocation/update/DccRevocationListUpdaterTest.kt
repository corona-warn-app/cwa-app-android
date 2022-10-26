package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import java.time.Instant

internal class DccRevocationListUpdaterTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var certificatesProvider: CertificateProvider
    @MockK lateinit var dccRevocationUpdateSettings: DccRevocationUpdateSettings
    @MockK lateinit var dccRevocationUpdateService: DccRevocationUpdateService

    private val vacc = mockk<VaccinationCertificate>()
    private val test = mockk<TestCertificate>()
    private val rec = mockk<RecoveryCertificate>()

    private val certificateSet = setOf(vacc, test, rec)
    private val allCertificatesFlow = MutableStateFlow(certificateSet)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        allCertificatesFlow.value = certificateSet
        every { certificatesProvider.allCertificates } returns allCertificatesFlow
        every { timeStamper.nowUTC } returns Instant.EPOCH

        coEvery { dccRevocationUpdateSettings.getLastUpdateTime() } returns Instant.EPOCH
        coEvery { dccRevocationUpdateSettings.setUpdateTimeToNow(any()) } just Runs
        coEvery { dccRevocationUpdateService.updateRevocationList(any()) } just Runs
    }

    @Test
    fun `update is not triggered on App start - first flow emission`() = runTest2 {
        getInstance(this)
        coVerify {
            dccRevocationUpdateSettings wasNot Called
            dccRevocationUpdateService wasNot Called
        }
    }

    @Test
    fun `update is not triggered when day is the same`() = runTest2 {
        getInstance(this).updateRevocationList(false)
        coVerify(exactly = 0) {
            dccRevocationUpdateService.updateRevocationList(any())
            dccRevocationUpdateSettings.setUpdateTimeToNow(any())
        }
    }

    @Test
    fun `update is triggered when day is different`() = runTest2 {
        every { timeStamper.nowUTC } returns Instant.parse("2000-01-01T00:00:00Z")
        getInstance(this).updateRevocationList(false)

        coVerify(exactly = 1) {
            dccRevocationUpdateService.updateRevocationList(any())
            dccRevocationUpdateSettings.setUpdateTimeToNow(any())
        }
    }

    @Test
    fun `update is triggered when force flag is on despite day check`() = runTest2 {
        getInstance(this).updateRevocationList(true)
        coVerify(exactly = 1) {
            dccRevocationUpdateService.updateRevocationList(any())
            dccRevocationUpdateSettings.setUpdateTimeToNow(any())
        }
    }

    @Test
    fun `update error is caught`() = runTest2 {
        coEvery { dccRevocationUpdateService.updateRevocationList(any()) } throws Exception("WOW!")
        shouldNotThrow<Exception> {
            getInstance(this).updateRevocationList(true)
        }
    }

    @Test
    fun `update is triggered only when size changes and calls update service with latest certificates`() =
        runTest2 {
            // Size is always 1 but set changes to make stateflow emit
            allCertificatesFlow.value = setOf(vacc)
            getInstance(scope = this)
            allCertificatesFlow.value = setOf(test)
            allCertificatesFlow.value = setOf(rec)

            coVerify(exactly = 0) {
                dccRevocationUpdateSettings.getLastUpdateTime()
                dccRevocationUpdateService.updateRevocationList(any())
                dccRevocationUpdateSettings.setUpdateTimeToNow(any())
            }

            // Size finally changes
            allCertificatesFlow.value = certificateSet

            coVerify(exactly = 1) {
                dccRevocationUpdateSettings.getLastUpdateTime()
                dccRevocationUpdateService.updateRevocationList(any())
                dccRevocationUpdateService.updateRevocationList(certificateSet)
                dccRevocationUpdateSettings.setUpdateTimeToNow(any())
            }
        }

    @Test
    fun `isUpdateRequired() should return true after one day`() = runTest2 {
        val updater = getInstance(this)

        // update is required when none was performed yet
        coEvery { dccRevocationUpdateSettings.getLastUpdateTime() } returns null
        updater.isUpdateRequired(Instant.now()) shouldBe true

        coEvery { dccRevocationUpdateSettings.getLastUpdateTime() } returns Instant.parse("2000-01-01T00:00:00Z")

        // no update required on the same day
        val sameDayBeginning = Instant.parse("2000-01-01T00:00:00Z")
        val sameDayEnd = Instant.parse("2000-01-01T23:59:59Z")
        updater.isUpdateRequired(sameDayBeginning) shouldBe false
        updater.isUpdateRequired(sameDayEnd) shouldBe false

        // update required on next day
        val nextDay = Instant.parse("2000-01-02T00:00:00Z")
        updater.isUpdateRequired(nextDay) shouldBe true

        // update should also happen on previous day (can happen when user fumbles with the device date)
        val previousDay = Instant.parse("1999-12-31T00:00:00Z")
        updater.isUpdateRequired(previousDay)
    }

    @Test
    fun `calls update service with given certificates or gets them from certificate provider`() =
        runTest2 {

            with(getInstance(scope = this)) {
                updateRevocationList(forceUpdate = true)
                coVerify { dccRevocationUpdateService.updateRevocationList(allCertificates = certificateSet) }

                val vaccSet = setOf(vacc)
                updateRevocationList(forceUpdate = true, allCertificates = vaccSet)
                coVerify { dccRevocationUpdateService.updateRevocationList(allCertificates = vaccSet) }

                val empty = emptySet<CwaCovidCertificate>()
                updateRevocationList(forceUpdate = true, allCertificates = empty)
                coVerify { dccRevocationUpdateService.updateRevocationList(allCertificates = empty) }
            }
        }

    private fun getInstance(scope: CoroutineScope): DccRevocationListUpdater {
        return DccRevocationListUpdater(
            appScope = scope,
            timeStamper = timeStamper,
            revocationUpdateSettings = dccRevocationUpdateSettings,
            certificatesProvider = certificatesProvider,
            revocationUpdateService = dccRevocationUpdateService
        )
    }
}
