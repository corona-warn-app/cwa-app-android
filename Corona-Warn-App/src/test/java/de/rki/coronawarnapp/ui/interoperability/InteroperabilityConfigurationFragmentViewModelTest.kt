package de.rki.coronawarnapp.ui.interoperability

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.ui.Country
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class InteroperabilityConfigurationFragmentViewModelTest {

    @MockK lateinit var interoperabilityRepository: InteroperabilityRepository

    @BeforeEach
    fun setupFreshViewModel() {
        MockKAnnotations.init(this)

        every { interoperabilityRepository.countryList } returns MutableLiveData(
            Country.values().toList()
        )
        every { interoperabilityRepository.getAllCountries() } just Runs
    }

    private fun createViewModel() =
        InteroperabilityConfigurationFragmentViewModel(interoperabilityRepository)

    @Test
    fun `viewmodel returns interop repo countryList`() {
        val vm = createViewModel()

        vm.countryList.getOrAwaitValue() shouldBe Country.values().toList()
    }

    @Test
    fun testFetchCountryList() {
        val vm = createViewModel()
        verify(exactly = 0) { interoperabilityRepository.getAllCountries() }
        vm.getAllCountries()
        verify(exactly = 1) { interoperabilityRepository.getAllCountries() }
    }

    @Test
    fun testBackPressButton() {
        val vm = createViewModel()
        vm.onBackPressed()

        vm.navigateBack.getOrAwaitValue() shouldBe true
    }
}
