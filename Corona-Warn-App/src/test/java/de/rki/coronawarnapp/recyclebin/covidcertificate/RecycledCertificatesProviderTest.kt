package de.rki.coronawarnapp.recyclebin.covidcertificate

import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2

class RecycledCertificatesProviderTest : BaseTest() {
    @MockK lateinit var vaccinationCertificateRepository: VaccinationCertificateRepository
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var recoveryCertificateRepository: RecoveryCertificateRepository

    private val tcContainerId = mockk<TestCertificateContainerId>()
    private val vcContainerId = mockk<VaccinationCertificateContainerId>()
    private val rcContainerId = mockk<RecoveryCertificateContainerId>()

    private val vc = mockk<VaccinationCertificate>()
        .apply {
            every { containerId } returns vcContainerId
            every { qrCodeToDisplay } returns CoilQrCode(content = "vcQrCode")
            every { recycledAt } returns Instant.parse("2020-06-20T23:00:00.000Z")
        }
    private val tc = mockk<TestCertificate>()
        .apply {
            every { containerId } returns tcContainerId
            every { qrCodeToDisplay } returns CoilQrCode(content = "tcQrCode")
            every { recycledAt } returns Instant.parse("2020-05-20T23:00:00.000Z")
        }
    private val rc = mockk<RecoveryCertificate>()
        .apply {
            every { containerId } returns rcContainerId
            every { qrCodeToDisplay } returns CoilQrCode(content = "rcQrCode")
            every { recycledAt } returns Instant.parse("2020-01-20T23:00:00.000Z")
        }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        with(recoveryCertificateRepository) {
            every { recycledCertificates } returns flowOf(setOf(rc))
            coEvery { restoreCertificate(any()) } just Runs
            coEvery { deleteCertificate(any()) } just Runs
        }

        with(testCertificateRepository) {
            every { recycledCertificates } returns flowOf(setOf(tc))
            coEvery { restoreCertificate(any()) } just Runs
            coEvery { deleteCertificate(any()) } returns null
        }

        with(vaccinationCertificateRepository) {
            every { recycledCertificates } returns flowOf(setOf(vc))
            coEvery { restoreCertificate(any()) } just Runs
            coEvery { deleteCertificate(any()) } just Runs
        }
    }

    @Test
    fun sorting() = runTest2 {
        provider(this).recycledCertificates.first() shouldBe setOf(vc, tc, rc)
    }

    @Test
    fun findCertificate() = runTest2 {
        val provider = provider(this)
        provider.findCertificate("tcQrCode") shouldBe tcContainerId
        provider.findCertificate("rcQrCode") shouldBe rcContainerId
        provider.findCertificate("vcQrCode") shouldBe vcContainerId
        provider.findCertificate("vcQrCode1") shouldBe null
    }

    @Test
    fun restoreCertificate() = runTest2 {
        val provider = provider(this)
        provider.restoreCertificate(tcContainerId)
        provider.restoreCertificate(vcContainerId)
        provider.restoreCertificate(rcContainerId)

        coVerify(exactly = 1) {
            testCertificateRepository.restoreCertificate(any())
            recoveryCertificateRepository.restoreCertificate(any())
            vaccinationCertificateRepository.restoreCertificate(any())
        }
    }

    @Test
    fun deleteCertificate() = runTest2 {
        val provider = provider(this)
        provider.deleteCertificate(tcContainerId)
        provider.deleteCertificate(vcContainerId)
        provider.deleteCertificate(rcContainerId)

        coVerify(exactly = 1) {
            testCertificateRepository.deleteCertificate(any())
            recoveryCertificateRepository.deleteCertificate(any())
            vaccinationCertificateRepository.deleteCertificate(any())
        }
    }

    @Test
    fun deleteAllCertificate() = runTest2 {
        provider(this).deleteAllCertificate(setOf(tcContainerId, vcContainerId, rcContainerId))

        coVerify(exactly = 1) {
            testCertificateRepository.deleteCertificate(any())
            recoveryCertificateRepository.deleteCertificate(any())
            vaccinationCertificateRepository.deleteCertificate(any())
        }
    }

    fun provider(scope: CoroutineScope) = RecycledCertificatesProvider(
        testCertificateRepository = testCertificateRepository,
        recoveryCertificateRepository = recoveryCertificateRepository,
        vaccinationCertificateRepository = vaccinationCertificateRepository,
        appScope = scope
    )
}
