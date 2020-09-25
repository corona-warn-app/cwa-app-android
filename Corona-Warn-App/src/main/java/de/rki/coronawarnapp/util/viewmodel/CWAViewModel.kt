package de.rki.coronawarnapp.util.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class CWAViewModel : ViewModel() {

    init {
        Timber.v("Initialized")
    }

    @CallSuper
    override fun onCleared() {
        viewModelScope.launch(context = Dispatchers.Default) { }
        Timber.v("onCleared()")
        super.onCleared()
    }
}
