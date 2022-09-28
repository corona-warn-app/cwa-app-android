package de.rki.coronawarnapp.contactdiary.model

import java.time.LocalDate

interface ContactDiaryPersonEncounter {
    val id: Long
    val date: LocalDate
    val contactDiaryPerson: ContactDiaryPerson
    val durationClassification: DurationClassification?
    val withMask: Boolean?
    val wasOutside: Boolean?
    val circumstances: String?

    enum class DurationClassification(
        val key: String
    ) {
        LESS_THAN_10_MINUTES("LessThan10Minutes"),
        MORE_THAN_10_MINUTES("MoreThan10Minutes")
    }
}

fun List<ContactDiaryPersonEncounter>.sortByNameAndIdASC(): List<ContactDiaryPersonEncounter> =
    this.sortedWith(
        compareBy(
            { it.contactDiaryPerson.fullName.lowercase() },
            { it.contactDiaryPerson.personId }
        )
    )
