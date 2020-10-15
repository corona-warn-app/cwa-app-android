package de.rki.coronawarnapp.util.ui

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("gone")
fun View.setGone(gone: Boolean) {
    visibility = if (gone) View.GONE else View.VISIBLE
}

fun View.setInvisible(invisible: Boolean) {
    visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}
