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
        /* _countriesActive.addSource(countries) { countries ->
             _countriesActive.value = countries.any { it.selected }
         }*/

        /*  _noInfoActive.addSource(countriesActive) {
              if (it) {
                  _noInfoActive.value = false
              }
          }*/

        /*  _nextButtonActive.addSource(countriesActive) {
              if (it) {
                  _nextButtonActive.value = true
              }
              if (!it && noInfoActive.value == false) {
                  _nextButtonActive.value = false
              }
          }*/

        _nextButtonActive.addSource(notSpecifiedActive) {
            if (it) {
                _nextButtonActive.value = true
            }
        }
    }


    fun positiveClick(){
        Timber.i("yes click")
    }

    fun negativeClick(){
        Timber.i("no click")
    }

    fun noInfoClick() {
        Timber.i("keine Angabe click")
        
    }

}





