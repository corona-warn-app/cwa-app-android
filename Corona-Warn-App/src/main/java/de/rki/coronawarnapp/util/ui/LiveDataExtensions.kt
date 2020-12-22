package de.rki.coronawarnapp.util.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner? = null, observer: Observer<T>) {
    val internalObserver = object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    }
    if (lifecycleOwner == null) {
        observeForever(internalObserver)
    } else {
        observe(lifecycleOwner, internalObserver)
    }
}
