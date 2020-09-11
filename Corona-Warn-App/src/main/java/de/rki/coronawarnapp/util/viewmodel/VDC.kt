package de.rki.coronawarnapp.util.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import timber.log.Timber

/**
 * Why is this called VDC?
 * Because ViewModel a confusing label for what **[ViewModel]** do, just Android Things (tm)
 * VDC stands for ViewDataController/Component because that's what it is.
 * A scoped component for view related data/logic.
 */
abstract class VDC : ViewModel() {
    val TAG: String = javaClass.simpleName

    init {
        Timber.tag(TAG).v("Initialized")
    }

    @CallSuper
    override fun onCleared() {
        Timber.tag(TAG).v("onCleared()")
        super.onCleared()
    }
}
