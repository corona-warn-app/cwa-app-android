package de.rki.coronawarnapp.ui.submission.viewmodel

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SubmissionContactViewModel @AssistedInject constructor() : CWAViewModel() {

    val navigateBack = SingleLiveEvent<Unit>()
    val dial = SingleLiveEvent<Unit>()
    val navigateToTan = SingleLiveEvent<Unit>()

    fun onBackPressed() {
        navigateBack.postValue(Unit)
    }

    fun onDialPressed() {
        dial.postValue(Unit)
    }

    fun onEnterTanPressed(){
        navigateToTan.postValue(Unit)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionContactViewModel>
}
