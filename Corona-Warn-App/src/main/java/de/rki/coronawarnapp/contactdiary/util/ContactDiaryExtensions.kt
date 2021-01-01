package de.rki.coronawarnapp.contactdiary.util

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.viewpager2.widget.ViewPager2
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.Locale

fun ViewPager2.registerOnPageChangeCallback(cb: (position: Int) -> Unit) {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            cb(position)
        }
    })
}

fun LocalDate.toFormattedDay(): String {
    // Use two different methods to get the final date format (Weekday, Shortdate)
    // because the custom pattern of toString() does not localize characters like "/" or "."
    return "${toString("EEEE", Locale.getDefault())}, " +
        DateTimeFormat.shortDate().withLocale(Locale.getDefault()).print(this)
}

fun String.formatContactDiaryNameField(maxLength: Int): String {
    val newName = if (isNotBlank()) {
        trim()
    } else {
        // allow only spaces as a name
        this
    }
    return newName.take(maxLength)
}

fun View.focusAndShowKeyboard() {
    /**
     * This is to be called when the window already has focus.
     */
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        showTheKeyboardNow()
    } else {
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}
