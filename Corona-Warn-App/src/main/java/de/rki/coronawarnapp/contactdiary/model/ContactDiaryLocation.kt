package de.rki.coronawarnapp.contactdiary.model

import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryLocationEntity
import de.rki.coronawarnapp.util.lists.HasStableId

interface ContactDiaryLocation : HasStableId {
    val locationId: Long
    var locationName: String
}

fun List<ContactDiaryLocation>.sortByNameAndIdASC(): List<ContactDiaryLocation> =
    this.sortedWith(compareBy({ it.locationName }, { it.locationId }))

fun ContactDiaryLocation.toEntity() = ContactDiaryLocationEntity(
    locationId, locationName
)
