package de.rki.coronawarnapp.contactdiary.model

import de.rki.coronawarnapp.util.lists.HasStableId
import java.util.Locale

interface ContactDiaryPerson : HasStableId {
    val personId: Long
    var fullName: String
    val phoneNumber: String?
    val emailAddress: String?
}

fun List<ContactDiaryPerson>.sortByNameAndIdASC(): List<ContactDiaryPerson> =
    this.sortedWith(compareBy({ it.fullName.lowercase()  }, { it.personId }))
