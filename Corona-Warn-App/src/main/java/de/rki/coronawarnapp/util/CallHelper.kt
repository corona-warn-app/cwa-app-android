package de.rki.coronawarnapp.util

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report

/**
 * Helper object for intents triggering a phone call
 * todo unify once necessary intents are final with share, external url and others
 */
object CallHelper {
    private val TAG: String? = CallHelper::class.simpleName

    fun call(fragment: Fragment, uri: String) {
        try {
            fragment.startActivity(
                Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse("tel:$uri")
                )
            )
        } catch (exception: Exception) {
            // catch generic exception on call
            // possibly due to bad number format
            exception.report(
                ExceptionCategory.UI,
                TAG,
                null
            )
        }
    }
}
