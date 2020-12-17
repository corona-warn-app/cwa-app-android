package de.rki.coronawarnapp.contactdiary.model

import de.rki.coronawarnapp.util.lists.HasStableId

interface ContactDiaryPerson : HasStableId {
    val personId: Long
    var fullName: String
}

fun List<ContactDiaryPerson>.sortByNameAndIdASC(): List<ContactDiaryPerson> =
    this.sortedWith(compareBy({ it.fullName }, { it.personId }))
