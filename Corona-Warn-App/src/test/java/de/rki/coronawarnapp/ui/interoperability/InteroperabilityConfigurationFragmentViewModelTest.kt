package de.rki.coronawarnapp.ui.interoperability

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@Extensions(
    ExtendWith(MockKExtension::class),
    ExtendWith(InstantExecutorExtension::class)
)
class InteroperabilityConfigurationFragmentViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var interoperabilityRepository: InteroperabilityRepository

    private lateinit var interoperabilityConfigurationFragmentViewModel: InteroperabilityConfigurationFragmentViewModel

    private val countryListLiveData = MutableLiveData<List<String>>(listOf())

    @BeforeEach
    fun setupFreshViewModel() {
        every { interoperabilityRepository.countryList } returns countryListLiveData
        interoperabilityConfigurationFragmentViewModel =
            InteroperabilityConfigurationFragmentViewModel(interoperabilityRepository)
    }

    @Test
    fun countryListIsEmptyIfRepositoryReturnsNoData() {
        val countryList =
            interoperabilityConfigurationFragmentViewModel.countryList.getOrAwaitValue()

        assertTrue(countryList.isEmpty())
    }

    @Test
    fun testFetchCountryList() {
        val countryListFetched = listOf(
            "DE", "UK", "FR", "IT", "ES", "PL", "RO", "NL",
            "BE", "CZ", "SE", "PT", "HU", "AT", "CH", "BG", "DK", "FI", "SK",
            "NO", "IE", "HR", "SI", "LT", "LV", "EE", "CY", "LU", "MT", "IS"
        )

        interoperabilityRepository.getAllCountries()
        countryListLiveData.value = countryListFetched

        val countryList =
            interoperabilityConfigurationFragmentViewModel.countryList.getOrAwaitValue()

        assertEquals(countryList.size, countryListFetched.size)
        assertTrue(countryList == countryListFetched)
        verify { interoperabilityRepository.getAllCountries() }
    }

    @Test
    fun testBackPressButton() {
        interoperabilityConfigurationFragmentViewModel.onBackPressed()

        assertTrue(interoperabilityConfigurationFragmentViewModel.navigateBack.getOrAwaitValue())
    }
}
