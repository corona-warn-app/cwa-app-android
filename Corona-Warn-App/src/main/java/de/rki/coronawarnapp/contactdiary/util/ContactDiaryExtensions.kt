package de.rki.coronawarnapp.contactdiary.util

import androidx.viewpager2.widget.ViewPager2

fun ViewPager2.registerOnPageChangeCallback(cb: (position: Int) -> Unit) {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            cb(position)
        }
    })
}
