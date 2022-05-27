package de.rki.coronawarnapp.contactdiary.model

import java.time.Duration
import java.time.LocalDate

data class DefaultContactDiaryLocationVisit(
    override val id: Long = 0L,
    override val date: LocalDate,
    override val contactDiaryLocation: ContactDiaryLocation,
    override val duration: Duration? = null,
    override val circumstances: String? = null,
    override val checkInID: Long? = null
) : ContactDiaryLocationVisit

fun ContactDiaryLocationVisit.toEditableVariant(): DefaultContactDiaryLocationVisit {
    if (this is DefaultContactDiaryLocationVisit) return this
    return DefaultContactDiaryLocationVisit(
        id = id,
        date = date,
        contactDiaryLocation = contactDiaryLocation,
        duration = duration,
        circumstances = circumstances,
        checkInID = checkInID
    )
}
