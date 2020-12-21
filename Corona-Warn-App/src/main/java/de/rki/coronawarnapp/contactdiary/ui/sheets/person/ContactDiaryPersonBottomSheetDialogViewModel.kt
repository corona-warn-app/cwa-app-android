package de.rki.coronawarnapp.contactdiary.ui.sheets.person

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate

class ContactDiaryPersonBottomSheetDialogViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val addedAt: String?,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val text = MutableStateFlow("")

    val isValid = text.map {
        it.isNotEmpty()
    }.asLiveData()

    val shouldClose = SingleLiveEvent<Unit>()

    private val formattedName: String
        get() {
            var newName = text.value
            //allow only spaces as a name
            if (newName.isNotBlank()) {
                newName = newName.trim()
            }
            return newName.take(MAX_PERSON_NAME_LENGTH)
        }

    fun textChanged(locationName: String) {
        text.value = locationName
    }

    fun addPerson() = launch {
        val person = contactDiaryRepository.addPerson(
            DefaultContactDiaryPerson(
                fullName = formattedName
            )
        )

        addedAt?.let {
            contactDiaryRepository.addPersonEncounter(
                DefaultContactDiaryPersonEncounter(
                    date = LocalDate.parse(it),
                    contactDiaryPerson = person
                )
            )
        }

        shouldClose.postValue(null)
    }

    fun updatePerson(person: ContactDiaryPersonEntity) = launch {
        contactDiaryRepository.updatePerson(
            DefaultContactDiaryPerson(
                person.personId,
                fullName = formattedName
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

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryPersonBottomSheetDialogViewModel> {
        fun create(addedAt: String?): ContactDiaryPersonBottomSheetDialogViewModel
    }
}

private const val MAX_PERSON_NAME_LENGTH = 250
