package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import de.rki.coronawarnapp.ccl.configuration.update.CclSettings
import de.rki.coronawarnapp.ccl.dccadmission.storage.DccAdmissionCheckScenariosRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.model.AdmissionScenario
import de.rki.coronawarnapp.covidcertificate.person.ui.admission.model.AdmissionScenarios
import de.rki.coronawarnapp.covidcertificate.person.ui.dccAdmissionCheckScenarios
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
internal class AdmissionScenariosViewModelTest : BaseTest() {

    @MockK lateinit var admissionCheckScenariosRepository: DccAdmissionCheckScenariosRepository
    @MockK lateinit var admissionScenariosSharedViewModel: AdmissionScenariosSharedViewModel
    @MockK lateinit var cclSetting: CclSettings
    @MockK lateinit var dccWalletInfoUpdateTrigger: DccWalletInfoUpdateTrigger
    private val admissionScenarios = AdmissionScenarios(
        title = "title",
        scenarios = listOf(
            AdmissionScenario(
                identifier = "BY",
                title = "Bayern",
                subtitle = "",
                enabled = true
            )
        ),
        scenariosAsJson = ""
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { admissionCheckScenariosRepository.admissionCheckScenarios } returns flowOf(dccAdmissionCheckScenarios)
        every { admissionScenariosSharedViewModel.admissionScenarios } returns flowOf(admissionScenarios)

        coEvery { admissionCheckScenariosRepository.save(any()) } just Runs
        coEvery { cclSetting.saveAdmissionScenarioId(any()) } returns Job()
        coEvery { dccWalletInfoUpdateTrigger.triggerNow(any()) } just Runs
    }

    @Test
    fun getState() {
        instance().state.getOrAwaitValue().apply {
            scenarios.size shouldBe admissionScenarios.scenarios.size
            title shouldBe admissionScenarios.title
            scenarios[0].apply {
                identifier shouldBe admissionScenarios.scenarios[0].identifier
                title shouldBe admissionScenarios.scenarios[0].title
                subtitle shouldBe admissionScenarios.scenarios[0].subtitle
                enabled shouldBe admissionScenarios.scenarios[0].enabled
            }
        }
    }

    @Test
    fun selectScenario() {
        instance().selectScenario("admissionScenarioId")
        coVerifySequence {
            admissionCheckScenariosRepository.save(any())
            cclSetting.saveAdmissionScenarioId(any())
            dccWalletInfoUpdateTrigger.triggerNow(any())
        }
    }

    private fun instance() = AdmissionScenariosViewModel(
        dispatcherProvider = TestDispatcherProvider(),
        admissionCheckScenariosRepository = admissionCheckScenariosRepository,
        admissionScenariosSharedViewModel = admissionScenariosSharedViewModel,
        cclSettings = cclSetting,
        dccWalletInfoUpdateTrigger = dccWalletInfoUpdateTrigger
    )
}
