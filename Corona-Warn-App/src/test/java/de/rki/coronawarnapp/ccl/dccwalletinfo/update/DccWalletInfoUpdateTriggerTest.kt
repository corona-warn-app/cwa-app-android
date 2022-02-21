package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.DccWalletInfoCleaner
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var cclSettings: CclSettings
    private val vc1 = mockk<VaccinationCertificate>().apply {
        every { qrCodeHash } returns "hash1"
    }

    private val vc2 = mockk<VaccinationCertificate>().apply {
        every { qrCodeHash } returns "hash2"
    }

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { dccWalletInfoCalculationManager.triggerNow(any()) } returns
            DccWalletInfoCalculationManager.Result.Success
        coEvery { dccWalletInfoCalculationManager.triggerAfterConfigChange(any()) } returns
            DccWalletInfoCalculationManager.Result.Success

        coEvery { dccWalletInfoCleaner.clean() } returns Result.success(Unit)
        every { personCertificateProvider.personCertificates } returns flowOf(setOf())
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { admissionScenariosEnabled } returns true
        }

        coEvery { cclSettings.getAdmissionScenarioId() } returns ""
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
                dccWalletInfoCalculationManager.triggerNow(any())
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
            dccWalletInfoCalculationManager.triggerNow(any())
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
                dccWalletInfoCalculationManager.triggerNow(any())
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
            dccWalletInfoCalculationManager.triggerNow(any())
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `No crash when update manager throw error`() = runBlockingTest2(true) {
        coEvery { dccWalletInfoCalculationManager.triggerNow(any()) } throws
            RuntimeException("error")
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))
        every { personCertificateProvider.personCertificates } returns flow
        shouldNotThrow<RuntimeException> {
            instance(this)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))
        }

        coVerify { dccWalletInfoCalculationManager.triggerNow(any()) }
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
    fun triggerAfterConfigChange() = runBlockingTest {
        instance(this).triggerAfterConfigChange(true)

        coVerify {
            dccWalletInfoCalculationManager.triggerAfterConfigChange(any(), any())
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `admissionScenarioId - enabled`() = runBlockingTest {

        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { admissionScenariosEnabled } returns true
        }

        coEvery { cclSettings.getAdmissionScenarioId() } returns "id"
        instance(this).admissionScenarioId() shouldBe "id"
    }

    @Test
    fun `admissionScenarioId - disabled`() = runBlockingTest {

        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { admissionScenariosEnabled } returns false
        }

        coEvery { cclSettings.getAdmissionScenarioId() } returns "id"
        instance(this).admissionScenarioId() shouldBe ""
    }

    private fun instance(scope: CoroutineScope) = DccWalletInfoUpdateTrigger(
        dccWalletInfoCalculationManager = dccWalletInfoCalculationManager,
        dccWalletInfoCleaner = dccWalletInfoCleaner,
        personCertificateProvider = personCertificateProvider,
        appScope = scope,
        appConfigProvider = appConfigProvider,
        cclSettings = cclSettings
    )
}
