package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.storage.RecoveryCertificateContainer
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.storage.TestCertificateContainer
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.storage.VaccinationCertificateContainer
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccQrCodeHandlerTest : BaseTest() {

    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var vaccinationCertificateRepository: VaccinationCertificateRepository
    @MockK lateinit var recoveryCertificateRepository: RecoveryCertificateRepository
    @MockK lateinit var dscSignatureValidator: DscSignatureValidator

    @MockK lateinit var testCertificateContainer: TestCertificateContainer
    @MockK lateinit var recoveryCertificateContainer: RecoveryCertificateContainer
    @MockK lateinit var vaccinationCertificateContainer: VaccinationCertificateContainer

    @MockK lateinit var testCertID: TestCertificateContainerId
    @MockK lateinit var recoveryCertID: RecoveryCertificateContainerId
    @MockK lateinit var vaccinationCertID: VaccinationCertificateContainerId

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { dscSignatureValidator.validateSignature(any(), any(), any()) } just Runs

        coEvery { testCertificateRepository.registerCertificate(any()) } returns testCertificateContainer
            .apply { every { containerId } returns testCertID }
        coEvery { vaccinationCertificateRepository.registerCertificate(any()) } returns vaccinationCertificateContainer
            .apply { every { containerId } returns vaccinationCertID }
        coEvery { recoveryCertificateRepository.registerCertificate(any()) } returns recoveryCertificateContainer
            .apply { every { containerId } returns recoveryCertID }

        coEvery { vaccinationCertificateRepository.recycleCertificate(any()) } just Runs
        coEvery { recoveryCertificateRepository.recycleCertificate(any()) } just Runs
        coEvery { testCertificateRepository.recycleCertificate(any()) } just Runs
    }

    @Test
    fun `handleQrCode calls Vax repo`() = runTest {
        val dccQrCode = mockk<VaccinationCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().validateAndRegister(dccQrCode) shouldBe vaccinationCertID
        coVerifySequence {
            dscSignatureValidator.validateSignature(any(), any(), any())
            vaccinationCertificateRepository.registerCertificate(any())
        }
    }

    @Test
    fun `handleQrCode calls Test repo`() = runTest {
        val dccQrCode = mockk<TestCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().validateAndRegister(dccQrCode) shouldBe testCertID
        coVerifySequence {
            dscSignatureValidator.validateSignature(any(), any(), any())
            testCertificateRepository.registerCertificate(any())
        }
    }

    @Test
    fun `handleQrCode calls Recovery repo`() = runTest {
        val dccQrCode = mockk<RecoveryCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().validateAndRegister(dccQrCode) shouldBe recoveryCertID
        coVerifySequence {
            dscSignatureValidator.validateSignature(any(), any(), any())
            recoveryCertificateRepository.registerCertificate(any())
        }
    }

    @Test
    fun `register calls vaccination repo`() = runTest {
        val dccQrCode = mockk<VaccinationCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().register(dccQrCode)
        coVerify {
            vaccinationCertificateRepository.registerCertificate(dccQrCode)
        }
    }

    @Test
    fun `register calls Test repo`() = runTest {
        val dccQrCode = mockk<TestCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().register(dccQrCode)
        coVerify {
            testCertificateRepository.registerCertificate(dccQrCode)
        }
    }

    @Test
    fun `register calls Recovery repo`() = runTest {
        val dccQrCode = mockk<RecoveryCertificateQRCode>().apply {
            every { data } returns mockk()
        }
        handler().register(dccQrCode)
        coVerify {
            recoveryCertificateRepository.registerCertificate(dccQrCode)
        }
    }

    @Test
    fun `move to recycle bin calls vaccination repo`() = runTest {
        val dccQrCode = mockk<VaccinationCertificateQRCode>().apply {
            every { data } returns mockk()
            every { hash } returns "hash"
        }
        handler().moveToRecycleBin(dccQrCode)
        coVerify {
            vaccinationCertificateRepository.recycleCertificate(
                VaccinationCertificateContainerId("hash")
            )
        }
    }

    @Test
    fun `move to recycle bin calls Test repo`() = runTest {
        val dccQrCode = mockk<TestCertificateQRCode>().apply {
            every { data } returns mockk()
            every { hash } returns "hash"
        }
        handler().moveToRecycleBin(dccQrCode)
        coVerify {
            testCertificateRepository.recycleCertificate(
                TestCertificateContainerId("hash")
            )
        }
    }

    @Test
    fun `move to recycle bin calls Recovery repo`() = runTest {
        val dccQrCode = mockk<RecoveryCertificateQRCode>().apply {
            every { data } returns mockk()
            every { hash } returns "hash"
        }
        handler().moveToRecycleBin(dccQrCode)
        coVerify {
            recoveryCertificateRepository.recycleCertificate(
                RecoveryCertificateContainerId("hash")
            )
        }
    }

    private fun handler() = DccQrCodeHandler(
        testCertificateRepository = testCertificateRepository,
        vaccinationCertificateRepository = vaccinationCertificateRepository,
        recoveryCertificateRepository = recoveryCertificateRepository,
        dscSignatureValidator = dscSignatureValidator,
    )
}
