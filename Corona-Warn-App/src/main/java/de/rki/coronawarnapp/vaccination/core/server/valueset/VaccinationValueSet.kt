package de.rki.coronawarnapp.vaccination.core.server.valueset

import java.util.Locale

interface VaccinationValueSet {
    val languageCode: Locale

    fun getDisplayText(key: String): String?
}
