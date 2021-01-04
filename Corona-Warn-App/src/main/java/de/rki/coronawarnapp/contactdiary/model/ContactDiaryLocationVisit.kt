package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate
import java.util.Locale

interface ContactDiaryLocationVisit {
    val id: Long
    val date: LocalDate
    val contactDiaryLocation: ContactDiaryLocation
}

fun List<ContactDiaryLocationVisit>.sortByNameAndIdASC(): List<ContactDiaryLocationVisit> =
    this.sortedWith(
        compareBy(
            { it.contactDiaryLocation.locationName.toLowerCase(Locale.ROOT) },
            { it.contactDiaryLocation.locationId }
        )
    )
