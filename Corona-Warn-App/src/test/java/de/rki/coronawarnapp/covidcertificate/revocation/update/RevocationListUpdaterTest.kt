package de.rki.coronawarnapp.covidcertificate.revocation.update

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificateProvider
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.revocation.storage.RevocationRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

internal class RevocationListUpdaterTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var certificatesProvider: CertificateProvider
    @MockK lateinit var revocationUpdateSettings: RevocationUpdateSettings
    @MockK lateinit var revocationRepository: RevocationRepository
    private val container = mockk<CertificateProvider.CertificateContainer>().apply {
        every { allCwaCertificates } returns setOf(
            mockk<VaccinationCertificate>(),
            mockk<TestCertificate>(),
            mockk<RecoveryCertificate>()
        )
    }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { certificatesProvider.allCertificatesSize } returns flowOf(5)
        every { certificatesProvider.certificateContainer } returns flowOf(container)
        every { timeStamper.nowUTC } returns Instant.EPOCH

        coEvery { revocationUpdateSettings.getLastUpdateTime() } returns Instant.EPOCH
        coEvery { revocationUpdateSettings.setUpdateTimeToNow(any()) } just Runs
        coEvery { revocationRepository.updateRevocationList(any()) } just Runs
    }

    @Test
    fun `update is not triggered on App start - first flow emission`() = runBlockingTest {
        getInstance(this)
        coVerify {
            revocationUpdateSettings wasNot Called
            revocationRepository wasNot Called
        }
    }

    @Test
    fun `update is not triggered when day is the same`() = runBlockingTest {
        getInstance(this).updateRevocationList(false)
        coVerify(exactly = 0) {
            revocationRepository.updateRevocationList(any())
            revocationUpdateSettings.setUpdateTimeToNow(any())
        }
    }

    @Test
    fun `update is triggered when day is different`() = runBlockingTest {
        every { timeStamper.nowUTC } returns Instant.parse("2000-01-01T00:00:00Z")
        getInstance(this).updateRevocationList(false)

        coVerify(exactly = 1) {
            revocationRepository.updateRevocationList(any())
            revocationUpdateSettings.setUpdateTimeToNow(any())
        }
    }

    @Test
    fun `update is triggered when force flag is on despite day check`() = runBlockingTest {
        getInstance(this).updateRevocationList(true)
        coVerify(exactly = 1) {
            revocationRepository.updateRevocationList(any())
            revocationUpdateSettings.setUpdateTimeToNow(any())
        }
    }

    @Test
    fun `update error is caught`() = runBlockingTest {
        coEvery { revocationRepository.updateRevocationList(any()) } throws Exception("WOW!")
        shouldNotThrow<Exception> {
            getInstance(this).updateRevocationList(true)
        }
    }

    @Test
    fun `update is triggered only when size changes`() = runBlockingTest2(ignoreActive = true) {
        val flow = MutableStateFlow(5)
        every { certificatesProvider.allCertificatesSize } returns flow
        getInstance(this)
        flow.apply {
            emit(5)
            delay(1_000)
            emit(5)
            delay(1_000)
            emit(5)
            delay(1_000)
            emit(6)
        }

        advanceTimeBy(3_000)

        coVerify(exactly = 1) {
            revocationUpdateSettings.getLastUpdateTime()
            revocationRepository.updateRevocationList(any())
            revocationUpdateSettings.setUpdateTimeToNow(any())
        }
    }

    @Test
    fun `isUpdateRequires() should return true after one day`() = runBlockingTest {
        val updater = getInstance(this)

        // update is required when none was performed yet
        coEvery { revocationUpdateSettings.getLastUpdateTime() } returns null
        updater.isUpdateRequired(Instant.now()) shouldBe true

        coEvery { revocationUpdateSettings.getLastUpdateTime() } returns Instant.parse("2000-01-01T00:00:00Z")

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

    private fun getInstance(scope: CoroutineScope): RevocationListUpdater {
        return RevocationListUpdater(
            appScope = scope,
            timeStamper = timeStamper,
            revocationUpdateSettings = revocationUpdateSettings,
            certificatesProvider = certificatesProvider,
            revocationRepository = revocationRepository
        )
    }
}
