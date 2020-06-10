package de.rki.coronawarnapp.util

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

/**
 * Helper object for intents triggering a phone call
 * todo unify once necessary intents are final with share, external url and others
 */
object CallHelper {
    fun call(fragment: Fragment, uri: String) {
        fragment.startActivity(
            Intent(
                Intent.ACTION_DIAL,
                Uri.parse(uri)
            )
        )
    }
}
