package de.rki.coronawarnapp.contactdiary.util

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.viewpager2.widget.ViewPager2
import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
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

fun Context.getLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        @Suppress("NewApi")
        resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    }
}

private fun patternEntry(locale: Locale): Pair<Locale, String> = Pair(locale, "EEEE, ${locale.shortDatePattern()}")

private val pattern_german: String

private val patterns = mapOf(
    patternEntry(Locale.GERMANY).apply { pattern_german = second },
    patternEntry(Locale.UK),
    patternEntry(Locale.US),
    patternEntry(Locale("bg", "BG")),
    patternEntry(Locale("pl", "PL")),
    patternEntry(Locale("bg", "BG")),
    patternEntry(Locale("ro", "RO")),
    patternEntry(Locale("tr", "TR"))
)

fun LocalDate.toFormattedDay(locale: Locale): String = toString(patterns[locale] ?: pattern_german, locale)

fun LocalDate.toFormattedDayForAccessibility(locale: Locale): String {
    // Use two different methods to get the final date format (Weekday, Longdate)
    // because the custom pattern of toString() does not localize characters like "/" or "."
    // For accessibility DateTimeFormat.longDate() is required since shortDate() may read the date in the wrong format
    return "${toString("EEEE", locale)}, " +
        DateTimeFormat.longDate().withLocale(locale).print(this)
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

fun View.setClickLabel(label: String) {
    ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityNodeInfo(v: View, info: AccessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(v, info)
            info.addAction(
                AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfoCompat.ACTION_CLICK, label
                )
            )
        }
    })
}

fun <T> MutableList<T>.clearAndAddAll(newItems: List<T>) {
    clear()
    addAll(newItems)
}
