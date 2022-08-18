package de.rki.coronawarnapp.util.ui

import android.content.Context
import android.os.Build
import android.view.Menu
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
