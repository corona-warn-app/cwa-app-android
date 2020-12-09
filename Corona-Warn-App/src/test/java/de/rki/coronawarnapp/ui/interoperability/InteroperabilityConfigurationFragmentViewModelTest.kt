package de.rki.coronawarnapp.ui.interoperability

import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.ui.Country
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class InteroperabilityConfigurationFragmentViewModelTest {

    @MockK lateinit var interopRepo: InteroperabilityRepository

    @BeforeEach
    fun setupFreshViewModel() {
        MockKAnnotations.init(this)

        every { interopRepo.countryList } returns flowOf(Country.values().toList())
    }

    private fun createViewModel() =
        InteroperabilityConfigurationFragmentViewModel(interopRepo, TestDispatcherProvider)

    @Test
    fun `viewmodel returns interop repo countryList`() {
        val vm = createViewModel()

        vm.countryList.getOrAwaitValue() shouldBe Country.values().toList()

        verify { interopRepo.countryList }
    }

    @Test
    fun `forced countrylist refresh via app config`() {
        val vm = createViewModel()
        coVerify(exactly = 0) { interopRepo.refreshCountries() }
        vm.refreshCountries()
        coVerify(exactly = 1) { interopRepo.refreshCountries() }
    }

    @Test
    fun testBackPressButton() {
        val vm = createViewModel()
        vm.onBackPressed()

        vm.navigateBack.getOrAwaitValue() shouldBe true
    }
}
