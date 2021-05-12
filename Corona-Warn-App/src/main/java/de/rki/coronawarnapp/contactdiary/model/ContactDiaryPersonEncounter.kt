package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

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
        LESS_THAN_15_MINUTES("LessThan15Minutes"),
        MORE_THAN_15_MINUTES("MoreThan15Minutes")
    }
}

fun List<ContactDiaryPersonEncounter>.sortByNameAndIdASC(): List<ContactDiaryPersonEncounter> =
    this.sortedWith(
        compareBy(
            { it.contactDiaryPerson.fullName.lowercase() },
            { it.contactDiaryPerson.personId }
        )
    )
