package de.rki.coronawarnapp.util

import android.content.Intent
import android.net.Uri
import de.rki.coronawarnapp.ui.BaseFragment

/**
 * Helper object for intents triggering a phone call
 * todo unify once necessary intents are final with share, external url and others
 */
object CallHelper {
    fun call(fragment: BaseFragment, uri: String) {
        fragment.startActivity(
            Intent(
                Intent.ACTION_DIAL,
                Uri.parse(uri)
            )
        )
    }
}
