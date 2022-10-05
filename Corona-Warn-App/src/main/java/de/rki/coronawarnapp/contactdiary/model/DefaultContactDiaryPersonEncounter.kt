package de.rki.coronawarnapp.contactdiary.model

import java.time.LocalDate

data class DefaultContactDiaryPersonEncounter(
    override val id: Long = 0L,
    override val date: LocalDate,
    override val contactDiaryPerson: ContactDiaryPerson,
    override val durationClassification: ContactDiaryPersonEncounter.DurationClassification? = null,
    override val withMask: Boolean? = null,
    override val wasOutside: Boolean? = null,
    override val circumstances: String? = null
) : ContactDiaryPersonEncounter

fun ContactDiaryPersonEncounter.toEditableVariant(): DefaultContactDiaryPersonEncounter {
    if (this is DefaultContactDiaryPersonEncounter) return this

    return DefaultContactDiaryPersonEncounter(
        id = id,
        date = date,
        contactDiaryPerson = contactDiaryPerson,
        durationClassification = durationClassification,
        withMask = withMask,
        wasOutside = wasOutside,
        circumstances = circumstances
    )
}
