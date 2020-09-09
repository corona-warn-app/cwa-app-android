package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel

class SubmissionTargetViewModel : ViewModel() {
    private val _currentButtonSelected = MediatorLiveData<String>()

    val currentButtonSelected: LiveData<String> = _currentButtonSelected

    init {
        _currentButtonSelected.value = ""
    }

    fun setCurrentButtonSelected(selectedButton: String) {
        _currentButtonSelected.postValue(selectedButton)
    }
}
