package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

data class DefaultContactDiaryPersonEncounter(
    override val id: Long = 0L,
    override val date: LocalDate,
    override val contactDiaryPerson: ContactDiaryPerson,
    override val durationClassification: ContactDiaryPersonEncounter.DurationClassification? = null,
    override val withMask: Boolean? = null,
    override val wasOutside: Boolean? = null,
    override val circumstances: String? = null
) : ContactDiaryPersonEncounter
