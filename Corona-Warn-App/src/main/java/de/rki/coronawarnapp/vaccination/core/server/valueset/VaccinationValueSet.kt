package de.rki.coronawarnapp.vaccination.core.server.valueset

import java.util.Locale

interface VaccinationValueSet {

    val languageCode: Locale
    val vp: ValueSet
    val mp: ValueSet
    val ma: ValueSet

    fun getDisplayText(key: String): String?

    interface ValueSet {
        val items: List<Item>

        fun getDisplayText(key: String): String?

        // Use custom class instead of map to allow for future extensions
        interface Item {
            val key: String
            val displayText: String
        }
    }
}
