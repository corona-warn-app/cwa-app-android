package de.rki.coronawarnapp.contactdiary.ui.edit

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.entity.toContactDiaryPersonEntity
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
class ContactDiaryEditPersonsViewModelTest {

    lateinit var viewModel: ContactDiaryEditPersonsViewModel
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    private val person = object : ContactDiaryPerson {
        override val personId = 1L
        override var fullName = "Julia"
        override val stableId = 1L
    }

    private val personList = listOf(person)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun testOnDeleteAllLocationsClick() {
        every { contactDiaryRepository.people } returns MutableStateFlow(personList)
        viewModel = ContactDiaryEditPersonsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.navigationEvent.observeForever { }
        viewModel.onDeleteAllPersonsClick()
        viewModel.navigationEvent.value shouldBe ContactDiaryEditPersonsViewModel.NavigationEvent.ShowDeletionConfirmationDialog
    }

    @Test
    fun testOnDeleteAllConfirmedClick() {
        coEvery { contactDiaryRepository.deleteAllPeople() } just Runs
        coEvery { contactDiaryRepository.deleteAllPersonEncounters() } just Runs
        every { contactDiaryRepository.people } returns MutableStateFlow(personList)
        viewModel = ContactDiaryEditPersonsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.onDeleteAllConfirmedClick()
        coVerify(exactly = 1) {
            contactDiaryRepository.deleteAllPeople()
            contactDiaryRepository.deleteAllPersonEncounters()
        }
    }

    @Test
    fun testOnEditLocationClick() {
        every { contactDiaryRepository.people } returns MutableStateFlow(personList)
        viewModel = ContactDiaryEditPersonsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.navigationEvent.observeForever { }
        viewModel.onEditPersonClick(person)
        viewModel.navigationEvent.value shouldBe
            ContactDiaryEditPersonsViewModel.NavigationEvent.ShowPersonDetailSheet(person.toContactDiaryPersonEntity())
    }

    @Test
    fun testIsButtonEnabled() {
        every { contactDiaryRepository.people } returns MutableStateFlow(personList)
        viewModel = ContactDiaryEditPersonsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.isButtonEnabled.observeForever { }
        viewModel.isButtonEnabled.value shouldBe true
    }

    @Test
    fun testIsButtonNotEnabledWhenListIsEmpty() {
        every { contactDiaryRepository.people } returns MutableStateFlow(emptyList())
        viewModel = ContactDiaryEditPersonsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.isButtonEnabled.observeForever { }
        viewModel.isButtonEnabled.value shouldBe false
    }

    @Test
    fun testLocations() {
        every { contactDiaryRepository.people } returns MutableStateFlow(personList)
        viewModel = ContactDiaryEditPersonsViewModel(contactDiaryRepository, TestDispatcherProvider)
        viewModel.personsLiveData.observeForever { }
        viewModel.personsLiveData.value shouldBe personList
    }
}
