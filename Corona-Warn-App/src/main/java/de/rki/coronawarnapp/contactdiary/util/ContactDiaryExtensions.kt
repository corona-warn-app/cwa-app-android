package de.rki.coronawarnapp.contactdiary.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

fun ViewPager2.registerOnPageChangeCallback(cb: (position: Int) -> Unit) {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            cb(position)
        }
    })
}

fun EditText.showKeyboard() = post {
        if (requestFocus()) context.inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

fun Fragment.hideKeyboard() {
    context?.inputMethodManager?.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    view?.clearFocus()
}

private val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
