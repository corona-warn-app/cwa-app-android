package de.rki.coronawarnapp.util

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

object OpenUrlHelper {
    fun navigate(fragment: Fragment, url: String) {
        fragment.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }
}
