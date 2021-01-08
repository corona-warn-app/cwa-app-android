package de.rki.coronawarnapp.contactdiary.model

import de.rki.coronawarnapp.util.lists.HasStableId
import java.util.Locale

interface ContactDiaryLocation : HasStableId {
    val locationId: Long
    var locationName: String
}

fun List<ContactDiaryLocation>.sortByNameAndIdASC(): List<ContactDiaryLocation> =
    this.sortedWith(compareBy({ it.locationName.toLowerCase(Locale.ROOT) }, { it.locationId }))
