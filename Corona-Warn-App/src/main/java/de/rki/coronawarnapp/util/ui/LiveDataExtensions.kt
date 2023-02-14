package de.rki.coronawarnapp.util.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner? = null, onValueChanged: (t: T) -> Unit) {
    val internalObserver = object : Observer<T> {
        override fun onChanged(t: T) {
            onValueChanged(t)
            removeObserver(this)
        }
    }
    if (lifecycleOwner == null) {
        observeForever(internalObserver)
    } else {
        observe(lifecycleOwner, internalObserver)
    }
}
