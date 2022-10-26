package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccwalletinfo.DccWalletInfoCleaner
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.DccWalletInfoCalculationManager
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import io.kotest.assertions.throwables.shouldNotThrow
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import java.time.Instant
import java.time.ZoneOffset

internal class DccWalletInfoUpdateTriggerTest : BaseTest() {

    @MockK lateinit var dccWalletInfoCalculationManager: DccWalletInfoCalculationManager
    @MockK lateinit var dccWalletInfoCleaner: DccWalletInfoCleaner
    @MockK lateinit var personCertificateProvider: PersonCertificatesProvider
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var cclSettings: CclSettings
    @MockK lateinit var personCertificatesSettings: PersonCertificatesSettings

    private val vc1 = mockk<VaccinationCertificate>().apply {
        every { qrCodeHash } returns "hash1"
        every { personIdentifier } returns mockk()
        every { vaccinatedOn } returns Instant.ofEpochMilli(10000L).atZone(ZoneOffset.UTC).toLocalDate()
        every { headerIssuedAt } returns Instant.ofEpochMilli(10000L)
    }

    private val vc2 = mockk<VaccinationCertificate>().apply {
        every { qrCodeHash } returns "hash2"
        every { personIdentifier } returns mockk()
        every { vaccinatedOn } returns Instant.ofEpochMilli(20000L).atZone(ZoneOffset.UTC).toLocalDate()
        every { headerIssuedAt } returns Instant.ofEpochMilli(20000L)
    }

    private val vc3 = mockk<VaccinationCertificate>().apply {
        every { qrCodeHash } returns "hash3"
        every { personIdentifier } returns mockk()
        every { vaccinatedOn } returns Instant.ofEpochMilli(30000L).atZone(ZoneOffset.UTC).toLocalDate()
        every { headerIssuedAt } returns Instant.ofEpochMilli(30000L)
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

        coEvery { cclSettings.admissionScenarioId() } returns ""
        coEvery { cclSettings.saveAdmissionScenarioId(any()) } returns Job()

        coEvery { personCertificatesSettings.cleanSettingsNotIn(any()) } just Runs
    }

    @Test
    fun `update triggered on first certificates change after initial empty set`() = runTest2(true) {
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf())))
        every { personCertificateProvider.personCertificates } returns flow
        instance(this)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        delay(1_00L)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        delay(1_100L)

        coVerify(exactly = 1) {
            dccWalletInfoCalculationManager.triggerNow(any())
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `update is not triggered on same certificates change`() = runTest2(true) {
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1))))
        every { personCertificateProvider.personCertificates } returns flow
        instance(this)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        delay(1_00L)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        delay(1_100L)

        coVerify(exactly = 1) {
            dccWalletInfoCalculationManager.triggerNow(any())
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `update is triggered on first certificates change after initial non empty set`() = runTest2(true) {
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1))))
        every { personCertificateProvider.personCertificates } returns flow
        instance(this)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))

        delay(1_00L)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))

        delay(1_100L)

        coVerify(exactly = 1) {
            dccWalletInfoCalculationManager.triggerNow(any())
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `update is triggered only once after rapid certificate changes`() = runTest2(true) {
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1))))
        every { personCertificateProvider.personCertificates } returns flow
        instance(this)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))

        delay(1_00L)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1, vc2, vc3))))

        delay(2_100L)

        coVerify(exactly = 1) {
            dccWalletInfoCalculationManager.triggerNow(any())
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `update is triggered on recycling certificate`() = runTest2(true) {
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))
        every { personCertificateProvider.personCertificates } returns flow
        instance(this)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        delay(1_00L)
        flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))

        delay(1_100L)

        coVerify(exactly = 1) {
            dccWalletInfoCalculationManager.triggerNow(any())
            dccWalletInfoCleaner.clean()
        }
    }

    @Test
    fun `No crash when update manager throw error`() = runTest2(true) {
        coEvery { dccWalletInfoCalculationManager.triggerNow(any()) } throws
            RuntimeException("error")
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))
        every { personCertificateProvider.personCertificates } returns flow
        shouldNotThrow<RuntimeException> {
            instance(this)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))
        }

        delay(1_100L)
        coVerify { dccWalletInfoCalculationManager.triggerNow(any()) }
    }

    @Test
    fun `No crash when update cleaner throw error`() = runTest2(true) {
        coEvery { dccWalletInfoCleaner.clean() } throws
            RuntimeException("error")
        val flow = MutableStateFlow(setOf(PersonCertificates(certificates = listOf(vc1, vc2))))
        every { personCertificateProvider.personCertificates } returns flow
        shouldNotThrow<RuntimeException> {
            instance(this)
            flow.emit(setOf(PersonCertificates(certificates = listOf(vc1))))
        }

        delay(1_100L)
        coVerify { dccWalletInfoCleaner.clean() }
    }

    @Test
    fun `triggerAfterConfigChange - feature is on and config update`() = runTest {
        coEvery { cclSettings.admissionScenarioId() } returns "BW"
        coEvery { dccWalletInfoCalculationManager.triggerAfterConfigChange("BW", true) } returns
            DccWalletInfoCalculationManager.Result.Success

        instance(this).triggerAfterConfigChange(true)

        coVerify {
            dccWalletInfoCalculationManager.triggerAfterConfigChange("BW", true)
            dccWalletInfoCleaner.clean()
            personCertificatesSettings.cleanSettingsNotIn(any())
        }
    }

    @Test
    fun `triggerAfterConfigChange - feature is off and no config update`() = runTest {
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { admissionScenariosEnabled } returns false
        }
        coEvery { dccWalletInfoCalculationManager.triggerAfterConfigChange("", false) } returns
            DccWalletInfoCalculationManager.Result.Success

        instance(this).triggerAfterConfigChange(false)

        coVerify {
            dccWalletInfoCalculationManager.triggerAfterConfigChange("", false)
            dccWalletInfoCleaner.clean()
            personCertificatesSettings.cleanSettingsNotIn(any())
        }
    }

    @Test
    fun triggerNow() = runTest {
        coEvery { dccWalletInfoCalculationManager.triggerNow("BW") } returns
            DccWalletInfoCalculationManager.Result.Success

        instance(this).triggerNow("BW")

        coVerify {
            dccWalletInfoCalculationManager.triggerNow("BW")
            dccWalletInfoCleaner.clean()
            personCertificatesSettings.cleanSettingsNotIn(any())
        }
    }

    private fun instance(scope: CoroutineScope) = DccWalletInfoUpdateTrigger(
        appScope = scope,
        cclSettings = cclSettings,
        dccWalletInfoCleaner = dccWalletInfoCleaner,
        appConfigProvider = appConfigProvider,
        personCertificateProvider = personCertificateProvider,
        personCertificatesSettings = personCertificatesSettings,
        dccWalletInfoCalculationManager = dccWalletInfoCalculationManager
    )
}
