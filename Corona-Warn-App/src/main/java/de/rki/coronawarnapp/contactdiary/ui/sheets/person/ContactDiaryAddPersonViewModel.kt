package de.rki.coronawarnapp.contactdiary.ui.sheets.person

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate

class ContactDiaryAddPersonViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val addedAt: String?,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, ex ->
        ex.report(ExceptionCategory.INTERNAL, TAG)
    }

    val shouldClose = SingleLiveEvent<Unit>()

    private val _text = MutableStateFlow("")
    val name: StateFlow<String> = _text

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber

    private val _emailAddress = MutableStateFlow("")
    val emailAddress: StateFlow<String> = _emailAddress

    val isValid = name
        .map { it.isNotEmpty() }
        .asLiveData()

    fun nameChanged(value: String) {
        _text.value = value.trim().take(MAX_PERSON_NAME_LENGTH)
    }

    fun phoneNumberChanged(value: String) {
        _phoneNumber.value = value.trim()
    }

    fun emailAddressChanged(value: String) {
        _emailAddress.value = value.trim()
    }

    fun addPerson() = launch(coroutineExceptionHandler) {
        val person = contactDiaryRepository.addEntries(
            DefaultContactDiaryPerson(
                fullName = name.value,
                phoneNumber = phoneNumber.value,
                emailAddress = emailAddress.value
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

    fun updatePerson(person: ContactDiaryPersonEntity) = launch(coroutineExceptionHandler) {
        contactDiaryRepository.updateEntries(
            DefaultContactDiaryPerson(
                person.personId,
                fullName = name.value
            )
        )
        shouldClose.postValue(null)
    }

    fun deletePerson(person: ContactDiaryPersonEntity) = launch(coroutineExceptionHandler) {
        contactDiaryRepository.personEncounters.firstOrNull()?.forEach {
            if (it.contactDiaryPerson.personId == person.personId)
                contactDiaryRepository.deletePersonEncounter(it)
        }
        contactDiaryRepository.deleteEntries(person)
        shouldClose.postValue(null)
    }

    fun closePressed() {
        shouldClose.postValue(null)
    }

    companion object {
        private const val MAX_PERSON_NAME_LENGTH = 250
        private val TAG = ContactDiaryAddPersonViewModel::class.java.simpleName
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ContactDiaryAddPersonViewModel> {
        fun create(addedAt: String?): ContactDiaryAddPersonViewModel
    }
}
