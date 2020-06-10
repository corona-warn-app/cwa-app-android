package de.rki.coronawarnapp.util

import android.content.Intent
import androidx.fragment.app.Fragment

object ShareHelper {
    fun shareText(fragment: Fragment, text: String, title: String?) {
        fragment.startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, title))
    }
}
