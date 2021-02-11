package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.LocalDate

data class DefaultContactDiaryLocationVisit(
    override val id: Long = 0L,
    override val date: LocalDate,
    override val contactDiaryLocation: ContactDiaryLocation,
    override val duration: Long? = null,
    override val circumstances: String? = null,
) : ContactDiaryLocationVisit
