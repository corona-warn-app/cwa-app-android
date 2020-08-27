package de.rki.coronawarnapp.ui.information

import de.rki.coronawarnapp.util.device.SystemInfoProvider
import org.junit.Assert
import org.junit.Test
import java.util.Locale

class InformationLegalPresentationTest {

    @Test
    fun test_showBackupLinkToContactForm() {
        // NO BACKUP NEEDED IN DE
        Assert.assertFalse(InformationLegalPresentation(object : SystemInfoProvider {
            override val locale: Locale
                get() = Locale.GERMAN
        }).showBackupLinkToContactForm)

        // NO BACKUP NEEDED IN EN
        Assert.assertFalse(InformationLegalPresentation(object : SystemInfoProvider {
            override val locale: Locale
                get() = Locale.ENGLISH
        }).showBackupLinkToContactForm)

        // BACKUP NEEDED IN FR
        Assert.assertTrue(InformationLegalPresentation(object : SystemInfoProvider {
            override val locale: Locale
                get() = Locale.FRENCH
        }).showBackupLinkToContactForm)

        // BACKUP NEEDED IN CN
        Assert.assertTrue(InformationLegalPresentation(object : SystemInfoProvider {
            override val locale: Locale
                get() = Locale.CHINESE
        }).showBackupLinkToContactForm)
    }
}
