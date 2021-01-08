package de.rki.coronawarnapp.contactdiary.model

import de.rki.coronawarnapp.util.lists.HasStableId
import java.util.Locale

interface ContactDiaryPerson : HasStableId {
    val personId: Long
    var fullName: String
}

fun List<ContactDiaryPerson>.sortByNameAndIdASC(): List<ContactDiaryPerson> =
    this.sortedWith(compareBy({ it.fullName.toLowerCase(Locale.ROOT) }, { it.personId }))
