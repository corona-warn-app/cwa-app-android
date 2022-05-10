package de.rki.coronawarnapp.util.ui

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import de.rki.coronawarnapp.util.recyclerview.ThrottledClickListener
import timber.log.Timber

@BindingAdapter("gone")
fun View.setGone(gone: Boolean) {
    visibility = if (gone) View.GONE else View.VISIBLE
}

@BindingAdapter("invisible")
fun View.setInvisible(invisible: Boolean) {
    visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}

fun View.setOnClickListenerThrottled(interval: Long = 300L, listenerBlock: (View) -> Unit) =
    setOnClickListener(ThrottledClickListener(interval, listenerBlock))

val View.hostLifecycle: Lifecycle?
    get() = try {
        findFragment<Fragment>().viewLifecycleOwner.lifecycle
    } catch (e: Exception) {
        Timber.v("Couldn't find viewLifecycleOwner for %s", this)
        null
    }

/**
 * Allows your view to receive callbacks from the host Fragment's lifecycle
 * Your callback is invoked when the owning Fragment/Activity receives the specified event state.
 *
 *  @param callback returns true if it should be consumed (one-time callback), or false if it was to stay registered.
 *
 * @return true if the callback has been added. Otherwise returns false,
 * i.e. if the view doesn't have a viewLifecycleOwner due to not being attached.
 */
fun View.addLifecycleEventCallback(
    type: Lifecycle.Event,
    callback: () -> Boolean
): Boolean {
    val hostLifecycle = hostLifecycle ?: return false

    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event != type) return
            Timber.v("%s triggered %s for %s", source, event, this@addLifecycleEventCallback)
            val consumed = callback()
            if (consumed) hostLifecycle.removeObserver(this)
        }
    }

    hostLifecycle.addObserver(observer)
    return true
}
