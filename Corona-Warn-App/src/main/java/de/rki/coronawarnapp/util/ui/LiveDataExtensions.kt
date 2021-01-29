package de.rki.coronawarnapp.util.ui

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.observe

fun <T> LiveData<T>.observe2(fragment: Fragment, callback: (T) -> Unit) {
    observe(fragment.viewLifecycleOwner) {
        callback.invoke(it)
    }
}

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
