package de.rki.coronawarnapp.contactdiary.model

interface ContactDiaryPerson {
    val personId: Long
    var fullName: String
}

fun List<ContactDiaryPerson>.sortByNameAndIdASC(): List<ContactDiaryPerson> =
    this.sortedWith(compareBy({ it.fullName }, { it.personId }))
