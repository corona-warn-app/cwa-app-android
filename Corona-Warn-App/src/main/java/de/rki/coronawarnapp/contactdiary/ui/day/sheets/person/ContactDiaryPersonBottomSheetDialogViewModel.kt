package de.rki.coronawarnapp.contactdiary.ui.day.sheets.person

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class ContactDiaryPersonBottomSheetDialogViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val shouldClose = SingleLiveEvent<Unit>()

    fun savePerson(personName: String) = launch {
        contactDiaryRepository.addDummyPerson(personName)
        shouldClose.postValue(null)
    }

    fun closePressed() {
        shouldClose.postValue(null)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<ContactDiaryPersonBottomSheetDialogViewModel>
}
