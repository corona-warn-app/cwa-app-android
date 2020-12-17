package de.rki.coronawarnapp.contactdiary.model

import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryPersonEntity
import de.rki.coronawarnapp.util.lists.HasStableId

interface ContactDiaryPerson : HasStableId {
    val personId: Long
    var fullName: String
}

fun List<ContactDiaryPerson>.sortByNameAndIdASC(): List<ContactDiaryPerson> =
    this.sortedWith(compareBy({ it.fullName }, { it.personId }))

fun ContactDiaryPerson.toEntity() = ContactDiaryPersonEntity(personId, fullName)
