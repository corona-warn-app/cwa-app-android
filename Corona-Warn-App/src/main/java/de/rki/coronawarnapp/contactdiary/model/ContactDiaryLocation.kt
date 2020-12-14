package de.rki.coronawarnapp.contactdiary.model

interface ContactDiaryLocation {
    val locationId: Long
    var locationName: String
}

fun List<ContactDiaryLocation>.sortByNameAndIdASC(): List<ContactDiaryLocation> =
    this.sortedWith(compareBy({ it.locationName }, { it.locationId }))
