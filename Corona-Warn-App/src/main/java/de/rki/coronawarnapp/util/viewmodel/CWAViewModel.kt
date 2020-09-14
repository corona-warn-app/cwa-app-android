package de.rki.coronawarnapp.util.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import timber.log.Timber

abstract class CWAViewModel : ViewModel() {

    init {
        Timber.v("Initialized")
    }

    @CallSuper
    override fun onCleared() {
        Timber.v("onCleared()")
        super.onCleared()
    }
}
