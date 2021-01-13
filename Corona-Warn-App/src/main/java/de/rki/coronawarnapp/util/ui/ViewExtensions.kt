package de.rki.coronawarnapp.util.ui

import android.view.View
import androidx.databinding.BindingAdapter
import de.rki.coronawarnapp.util.recyclerview.ThrottledClickListener

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
