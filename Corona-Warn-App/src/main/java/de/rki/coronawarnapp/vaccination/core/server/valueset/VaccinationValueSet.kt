package de.rki.coronawarnapp.vaccination.core.server.valueset

import java.util.Locale

interface VaccinationValueSet {
    val languageCode: Locale
    val vp: ValueSet
    val mp: ValueSet
    val ma: ValueSet

    interface ValueSet {
        val items: List<Item>

        // Use custom item instead of map to allow for future extensions
        interface Item {
            val key: String
            val displayText: String
        }
    }
}

fun VaccinationValueSet.getDisplayText(key: String): String? =
    vp.getDisplayText(key) ?: mp.getDisplayText(key) ?: ma.getDisplayText(key)

fun VaccinationValueSet.ValueSet.getDisplayText(key: String): String? = items.find { key == it.key }?.displayText
