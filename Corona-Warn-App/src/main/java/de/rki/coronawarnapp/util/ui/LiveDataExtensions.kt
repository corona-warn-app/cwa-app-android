package de.rki.coronawarnapp.util.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observe2(fragment: Fragment, callback: (T) -> Unit) {
    observe(fragment.viewLifecycleOwner, Observer { callback.invoke(it) })
}

fun <T> LiveData<T>.observe2(activity: AppCompatActivity, callback: (T) -> Unit) {
    observe(activity, Observer { callback.invoke(it) })
}
