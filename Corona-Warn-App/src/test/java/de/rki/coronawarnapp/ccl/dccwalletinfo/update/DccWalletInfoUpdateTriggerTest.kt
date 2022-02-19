package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.ccl.dccwalletinfo.DccWalletInfoCleaner
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.task.TaskController
import io.kotest.assertions.throwables.shouldNotThrow
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

internal class DccWalletInfoUpdateTriggerTest : BaseTest() {

    @MockK lateinit var dccWalletInfoCalculationManager: DccWalletInfoCalculationManager
    @MockK lateinit var dccWalletInfoCleaner: DccWalletInfoCleaner
    @MockK lateinit var personCertificateProvider: PersonCertificatesProvider
    @MockK lateinit var taskController: TaskController
    private val vc1 = mockk<VaccinationCertificate>().apply {
        every { qrCodeHash } returns "hash1"
    }

    private val vc2 = mockk<VaccinationCertificate>().apply {
        every { qrCodeHash } returns "hash2"
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { taskController.submit(any()) } just Runs
        coEvery { dccWalletInfoCalculationManager.triggerCalculationAfterCertificateChange() } just Runs
        coEvery { dccWalletInfoCalculationManager.triggerCalculationAfterConfigChange(any()) } just Runs
        coEvery { dccWalletInfoCleaner.clean() } just Runs
        every { personCertificateProvider.personCertificates } returns flowOf(setOf())
    }

    @Test
    fun `update triggered on first certificates change after initial empty set`() =
        runBlockingTest2(true) {
            val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf())))
            every { personCertificateProvider.personCertificates } returns flow
            instance(this)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

            delay(1_00L)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

            coVerify(exactly = 1) {
                dccWalletInfoCalculationManager.triggerCalculationAfterCertificateChange()
                dccWalletInfoCleaner.clean()
            }
        }

    @Test
    fun `update is not triggered on same certificates change`() = runBlockingTest2(true) {
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1))))
        every { personCertificateProvider.personCertificates } returns flow
        instance(this)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        delay(1_00L)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        coVerify(exactly = 0) {
            dccWalletInfoCalculationManager.triggerCalculationAfterCertificateChange()
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `update is triggered on first certificates change after initial non empty set`() =
        runBlockingTest2(true) {
            val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1))))
            every { personCertificateProvider.personCertificates } returns flow
            instance(this)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))

            delay(1_00L)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))

            coVerify(exactly = 1) {
                dccWalletInfoCalculationManager.triggerCalculationAfterCertificateChange()
                dccWalletInfoCleaner.clean()
            }
        }

    @Test
    fun `update is triggered on recycling certificate`() = runBlockingTest2(true) {
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))
        every { personCertificateProvider.personCertificates } returns flow
        instance(this)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        delay(1_00L)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        coVerify(exactly = 1) {
            dccWalletInfoCalculationManager.triggerCalculationAfterCertificateChange()
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `No crash when update manager throw error`() = runBlockingTest2(true) {
        coEvery { dccWalletInfoCalculationManager.triggerCalculationAfterCertificateChange() } throws
            RuntimeException("error")
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))
        every { personCertificateProvider.personCertificates } returns flow
        shouldNotThrow<RuntimeException> {
            instance(this)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))
        }

        coVerify { dccWalletInfoCalculationManager.triggerCalculationAfterCertificateChange() }
    }

    @Test
    fun `No crash when update cleaner throw error`() = runBlockingTest2(true) {
        coEvery { dccWalletInfoCleaner.clean() } throws
            RuntimeException("error")
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))
        every { personCertificateProvider.personCertificates } returns flow
        shouldNotThrow<RuntimeException> {
            instance(this)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))
        }
        coVerify { dccWalletInfoCleaner.clean() }
    }

    @Test
    fun `triggerDccWalletInfoUpdateAfterConfigUpdate false`() = runBlockingTest {
        instance(this).triggerDccWalletInfoUpdateAfterConfigUpdate(false)
        verify { taskController.submit(any()) }
    }

    @Test
    fun `triggerDccWalletInfoUpdateAfterConfigUpdate true`() = runBlockingTest {
        instance(this).triggerDccWalletInfoUpdateAfterConfigUpdate(true)
        verify { taskController.submit(any()) }
    }

    private fun instance(scope: CoroutineScope) = DccWalletInfoUpdateTrigger(
        dccWalletInfoCalculationManager = dccWalletInfoCalculationManager,
        dccWalletInfoCleaner = dccWalletInfoCleaner,
        personCertificateProvider = personCertificateProvider,
        appScope = scope,
        taskController = taskController
    )
}
