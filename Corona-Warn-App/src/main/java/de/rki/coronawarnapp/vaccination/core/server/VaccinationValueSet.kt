package de.rki.coronawarnapp.vaccination.core.server

import java.util.Locale

interface VaccinationValueSet {
    val languageCode: Locale

    fun getDisplayText(key: String): String?
}
