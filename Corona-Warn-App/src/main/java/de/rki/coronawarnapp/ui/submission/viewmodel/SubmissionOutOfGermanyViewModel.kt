package de.rki.coronawarnapp.ui.submission.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class SubmissionOutOfGermanyViewModel : ViewModel() {


    private val _yesAnswerActive = MediatorLiveData<Boolean>()
    private val _noAnswerActive = MediatorLiveData<Boolean>()
    private val _notSpecifiedActive = MediatorLiveData<Boolean>()
    private val _nextButtonActive = MediatorLiveData<Boolean>()


    val yesAnswerActive: LiveData<Boolean> = _yesAnswerActive
    val noAnswerActive: LiveData<Boolean> = _noAnswerActive
    val notSpecifiedActive: LiveData<Boolean> = _notSpecifiedActive
    val nextActive: LiveData<Boolean> = _nextButtonActive

    init {
        _nextButtonActive.addSource(yesAnswerActive ) {
            if (it) { _nextButtonActive.value = true }
        }
        _nextButtonActive.addSource(noAnswerActive ) {
            if (it) { _nextButtonActive.value = true }
        }
        _nextButtonActive.addSource(notSpecifiedActive ) {
            if (it) { _nextButtonActive.value = true }
        }
    }


    fun positiveClick(){
        _yesAnswerActive.postValue(true)
        _noAnswerActive.postValue(false)
        _notSpecifiedActive.postValue(false)
    }

    fun negativeClick(){
        _noAnswerActive.postValue(true)
        _yesAnswerActive.postValue(false)
        _notSpecifiedActive.postValue(false)
    }

    fun noInfoClick() {
        _notSpecifiedActive.postValue(true)
        _yesAnswerActive.postValue(false)
        _noAnswerActive.postValue(false)

    }

}





