package de.rki.coronawarnapp.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Helper to supply live data that can only be observed once
 */
open class Event<out T>(private val eventContent: T) {
    private var handled = false

    fun getContent(): T? {
        return if (handled) {
            null
        } else {
            handled = true
            eventContent
        }
    }
}

inline fun <T> LiveData<Event<T>>.observeEvent(
    owner: LifecycleOwner,
    crossinline onEvent: (T) -> Unit
) {
    observe(owner, Observer { it?.getContent()?.let(onEvent) })
}
