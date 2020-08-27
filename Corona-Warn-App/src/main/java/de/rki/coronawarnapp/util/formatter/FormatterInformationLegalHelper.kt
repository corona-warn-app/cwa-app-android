@file:JvmName("FormatterInformationLegalHelper")

package de.rki.coronawarnapp.util.formatter

import android.view.View
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.ui.information.InformationLegalPresentation
import de.rki.coronawarnapp.util.device.DefaultSystemInfoProvider

/**
 * Language based format visibility
 *
 * @param isContactFormView
 * @return
 */

fun formatVisibilityLanguageBased(
    isContactFormView: Boolean?
): Int {
    InformationLegalPresentation(DefaultSystemInfoProvider(CoronaWarnApplication.getAppContext())).apply {
        if (!showBackupLinkToContactForm) {
            return if (isContactFormView == true) {
                View.VISIBLE
            } else View.GONE
        }
        return if (isContactFormView == false) {
            View.VISIBLE
        } else View.GONE
    }
}
