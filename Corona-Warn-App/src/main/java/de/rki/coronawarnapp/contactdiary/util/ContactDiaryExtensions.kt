package de.rki.coronawarnapp.contactdiary.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import org.joda.time.LocalDate
import java.util.Locale

fun ViewPager2.registerOnPageChangeCallback(cb: (position: Int) -> Unit) {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            cb(position)
        }
    })
}

// According to tech spec german locale only
fun LocalDate.toFormattedDay(): String = toString("EEEE, dd.MM.yy", Locale.GERMAN)

fun EditText.showKeyboard(view: View) = post {
    if (requestFocus()) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }
}

fun Fragment.hideKeyboard() {
    context?.inputMethodManager?.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    view?.clearFocus()
}

private val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
