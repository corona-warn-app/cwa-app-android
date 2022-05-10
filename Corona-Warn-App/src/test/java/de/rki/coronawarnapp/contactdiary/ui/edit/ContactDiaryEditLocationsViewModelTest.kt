package de.rki.coronawarnapp.contactdiary.ui.edit

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryLocationEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
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
        override val phoneNumber: String? = null
        override val emailAddress: String? = null
        override val stableId = 1L
        override val traceLocationID: TraceLocationId? = null
    }
    private val locationList = listOf(location)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    fun createInstance() = ContactDiaryEditLocationsViewModel(
        appScope = TestScope(),
        contactDiaryRepository = contactDiaryRepository,
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun testOnDeleteAllLocationsClick() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = createInstance()
        viewModel.navigationEvent.observeForever { }
        viewModel.onDeleteAllLocationsClick()
        viewModel.navigationEvent.value shouldBe
            ContactDiaryEditLocationsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
    }

    @Test
    fun testOnDeleteAllConfirmedClick() {
        coEvery { contactDiaryRepository.deleteAllLocationVisits() } just Runs
        coEvery { contactDiaryRepository.deleteAllLocations() } just Runs
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = createInstance()
        viewModel.onDeleteAllConfirmedClick()
        coVerify(exactly = 1) {
            contactDiaryRepository.deleteAllLocationVisits()
            contactDiaryRepository.deleteAllLocations()
        }
    }

    @Test
    fun testOnEditLocationClick() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = createInstance()
        viewModel.navigationEvent.observeForever { }
        viewModel.onEditLocationClick(location)
        viewModel.navigationEvent.value shouldBe
            ContactDiaryEditLocationsViewModel.NavigationEvent.ShowLocationDetailFragment(
                location.toContactDiaryLocationEntity()
            )
    }

    @Test
    fun testIsButtonEnabled() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = createInstance()
        viewModel.isButtonEnabled.observeForever { }
        viewModel.isButtonEnabled.value shouldBe true
    }

    @Test
    fun testIsButtonNotEnabledWhenListIsEmpty() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(emptyList())
        viewModel = createInstance()
        viewModel.isButtonEnabled.observeForever { }
        viewModel.isButtonEnabled.value shouldBe false
    }

    @Test
    fun testLocations() {
        every { contactDiaryRepository.locations } returns MutableStateFlow(locationList)
        viewModel = createInstance()
        viewModel.locationsLiveData.observeForever { }
        viewModel.locationsLiveData.value shouldBe locationList
    }
}
