package de.rki.coronawarnapp.contactdiary.util

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
fun LocalDate.toFormattedDay() = toString("EEEE, dd.MM.yyyy", Locale.GERMAN)
