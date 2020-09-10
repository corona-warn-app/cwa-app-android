package de.rki.coronawarnapp.util.device

import java.util.Locale

interface SystemInfoProvider {

    /**
     * the device Locale
     */
    val locale: Locale
}
