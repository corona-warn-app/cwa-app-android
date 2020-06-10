package de.rki.coronawarnapp.util

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report

object OpenUrlHelper {
    private val TAG: String? = OpenUrlHelper::class.simpleName

    fun navigate(fragment: Fragment, url: String) {
        try {
            fragment.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        } catch (exception: Exception) {
            // catch generic exception on url navigation
            // most likely due to bad url format
            // or less likely no browser installed
            exception.report(
                ExceptionCategory.UI,
                TAG,
                null
            )
        }
    }
}
