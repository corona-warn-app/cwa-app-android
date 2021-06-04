package de.rki.coronawarnapp.vaccination.core.server.valueset.valuesets

import java.util.Locale

interface ValueSets {

    val languageCode: Locale

    // Disease or agent targeted
    val tg: ValueSet

    interface ValueSet {
        val items: List<Item>

        // Use custom item instead of map to allow for future extensions
        interface Item {
            val key: String
            val displayText: String
        }
    }
}
