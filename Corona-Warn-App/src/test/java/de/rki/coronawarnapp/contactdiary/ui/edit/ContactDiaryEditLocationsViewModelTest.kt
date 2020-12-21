package de.rki.coronawarnapp.contactdiary.ui.edit

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class ContactDiaryEditLocationsViewModelTest {

    lateinit var viewModel: ContactDiaryEditLocationsViewModel
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    private val location = object : ContactDiaryLocation {
        override val locationId = 1L
        override var locationName = "Supermarket"
        override val stableId = 1L
    }
    private val locationList = listOf(location)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testOnDeleteAllLocationsClick() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = ContactDiaryEditLocationsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.navigationEvent.observeForever { }
        viewModel.onDeleteAllLocationsClick()
        viewModel.navigationEvent.value shouldBe ContactDiaryEditLocationsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
    }

    @Test
    fun testOnDeleteAllConfirmedClick() {
        coEvery { contactDiaryRepository.deleteAllLocationVisits() } just Runs
        coEvery { contactDiaryRepository.deleteAllLocations() } just Runs
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = ContactDiaryEditLocationsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.onDeleteAllConfirmedClick()
        coVerify(exactly = 1) {
            contactDiaryRepository.deleteAllLocationVisits()
            contactDiaryRepository.deleteAllLocations()
        }
    }

    @Test
    fun testOnEditLocationClick() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = ContactDiaryEditLocationsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.navigationEvent.observeForever { }
        viewModel.onEditLocationClick(location)
        viewModel.navigationEvent.value shouldBe
            ContactDiaryEditLocationsViewModel.NavigationEvent.ShowLocationDetailSheet(location.toContactDiaryLocationEntity())
    }

    @Test
    fun testIsButtonEnabled() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = ContactDiaryEditLocationsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.isButtonEnabled.observeForever { }
        viewModel.isButtonEnabled.value shouldBe true
    }

    @Test
    fun testIsButtonNotEnabledWhenListIsEmpty() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(emptyList())
        viewModel = ContactDiaryEditLocationsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.isButtonEnabled.observeForever { }
        viewModel.isButtonEnabled.value shouldBe false
    }

    @Test
    fun testLocations() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = ContactDiaryEditLocationsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.locationsLiveData.observeForever { }
        viewModel.locationsLiveData.value shouldBe locationList
    }
}
