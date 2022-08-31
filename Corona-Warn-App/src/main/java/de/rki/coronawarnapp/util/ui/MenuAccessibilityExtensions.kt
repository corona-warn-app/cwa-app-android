package de.rki.coronawarnapp.util.ui

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.Menu
import androidx.core.view.size
import de.rki.coronawarnapp.R

fun Menu.setItemContentDescription(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        for (i in 0 until size()) {
            val item = getItem(i)
            item.contentDescription = context.getString(
                R.string.menu_item_accessibility_description,
                item.title.toString(),
                i + 1,
                size()
            )
        }
    }
}

fun Menu.setTextSize() {
    for (i in 0 until size) {
        val spanString = SpannableString(getItem(i).title)
        spanString.setSpan(StyleSpan(R.style.body2), 0, spanString.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        getItem(i).title = spanString
    }
}
