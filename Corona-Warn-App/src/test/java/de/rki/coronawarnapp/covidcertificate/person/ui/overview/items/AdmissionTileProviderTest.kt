package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.ccl.configuration.update.CCLSettings
import de.rki.coronawarnapp.ccl.dccadmission.model.storage.DccAdmissionCheckScenariosRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.calculation.CCLJsonFunctions
import de.rki.coronawarnapp.ccl.ui.text.CCLTextFormatter
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.dccAdmissionCheckScenarios
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewViewModel
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class AdmissionTileProviderTest : BaseTest() {
    @MockK lateinit var admissionCheckScenariosRepository: DccAdmissionCheckScenariosRepository
    @MockK lateinit var cclSettings: CCLSettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var cclJsonFunctions: CCLJsonFunctions
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider

    private val mapper = SerializationModule.jacksonBaseMapper

    @BeforeEach
    fun setup() {
        every { admissionCheckScenariosRepository.admissionCheckScenarios } returns flowOf(mockk())
        every { configData.admissionScenariosDisabled } returns false
        every { appConfigProvider.currentConfig } returns flowOf(configData)
        every { cclSettings.admissionScenarioId } returns flowOf("DE")
        every { admissionCheckScenariosRepository.admissionCheckScenarios } returns flowOf(dccAdmissionCheckScenarios)
        every { personCertificatesProvider.personCertificates } returns flowOf(
            setOf(
                PersonCertificates(
                    certificates = listOf(),
                    isCwaUser = true,
                    dccWalletInfo = null
                )
            )
        )
    }

    @Test
    fun `admission tile is not visible when config flag is disabled`() = runBlockingTest {
        every { configData.admissionScenariosDisabled } returns true
        instance().run {
            admissionTile.first() shouldBe PersonOverviewViewModel.AdmissionTile(
                visible = false,
                title = "Status anzeigen für folgendes Bundesland:",
                subtitle = "Bundesweit"
            )
        }
    }

    @Test
    fun `admission tile is not visible when no certificates are available`() = runBlockingTest {
        every { personCertificatesProvider.personCertificates } returns flowOf(setOf())
        instance().run {
            admissionTile.first() shouldBe PersonOverviewViewModel.AdmissionTile(
                visible = false,
                title = "Status anzeigen für folgendes Bundesland:",
                subtitle = "Bundesweit"
            )
        }
    }

    @Test
    fun `admission tile - subtitle is from selected scenario`() = runBlockingTest {
        every { cclSettings.admissionScenarioId } returns flowOf("BW")
        instance().run {
            admissionTile.first() shouldBe PersonOverviewViewModel.AdmissionTile(
                visible = true,
                title = "Status anzeigen für folgendes Bundesland:",
                subtitle = "Baden-Württemberg"
            )
        }
    }

    fun instance() = AdmissionTileProvider(
        format = CCLTextFormatter(cclJsonFunctions, mapper),
        appConfigProvider = appConfigProvider,
        certificatesProvider = personCertificatesProvider,
        admissionCheckScenariosRepository = admissionCheckScenariosRepository,
        cclSettings = cclSettings
    )
}
