package de.rki.coronawarnapp.util

import android.content.Intent
import de.rki.coronawarnapp.ui.BaseFragment

object ShareHelper {
    fun shareText(fragment: BaseFragment, text: String, title: String?) {
        fragment.startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, title))
    }
}
