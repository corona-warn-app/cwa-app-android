package de.rki.coronawarnapp.ui.information

import de.rki.coronawarnapp.util.device.SystemInfoProvider
import java.util.Locale
import javax.inject.Inject

class InformationLegalPresentation @Inject constructor(private val systemInfoProvider: SystemInfoProvider) {

    val showBackupLinkToContactForm: Boolean
        get() {
            systemInfoProvider.locale.apply {
                return language != Locale.ENGLISH.language && language != Locale.GERMAN.language
            }
        }
}
