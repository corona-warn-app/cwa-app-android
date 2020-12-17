package de.rki.coronawarnapp.contactdiary.ui.day.sheets.person

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class ContactDiaryPersonBottomSheetDialogViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val text = MutableStateFlow("")

    val isValid = text.map {
        it.isNotEmpty() && it.length <= MAX_PERSON_NAME_LENGTH
    }.asLiveData()

    val shouldClose = SingleLiveEvent<Unit>()

    fun textChanged(locationName: String) {
        text.value = locationName
    }

    fun addPerson() = launch {
        contactDiaryRepository.addPerson(
            DefaultContactDiaryPerson(
                fullName = text.value.take(MAX_PERSON_NAME_LENGTH)
            )
        )
        shouldClose.postValue(null)
    }

    fun updatePerson(person: ContactDiaryPersonEntity) = launch {
        contactDiaryRepository.updatePerson(
            ContactDiaryPersonEntity(
                person.personId,
                fullName = text.value.take(MAX_PERSON_NAME_LENGTH)
            )
        )
        shouldClose.postValue(null)
    }

    fun deletePerson(person: ContactDiaryPersonEntity) = launch {
        contactDiaryRepository.personEncounters.firstOrNull()?.forEach {
            if (it.contactDiaryPerson.personId == person.personId)
                contactDiaryRepository.deletePersonEncounter(it)
        }
        contactDiaryRepository.deletePerson(person)
        shouldClose.postValue(null)
    }

    fun closePressed() {
        shouldClose.postValue(null)
    }

    companion object {
        private const val MAX_PERSON_NAME_LENGTH = 250
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryPersonBottomSheetDialogViewModel>
}
