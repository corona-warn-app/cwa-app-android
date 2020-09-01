package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EfgsConsentViewModel : ViewModel() {

    companion object {
        val TAG: String? = TracingViewModel::class.simpleName
    }

    val isEfgsConsentGranted: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
}
