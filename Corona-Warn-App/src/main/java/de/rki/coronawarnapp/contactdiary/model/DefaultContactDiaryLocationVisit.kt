package de.rki.coronawarnapp.contactdiary.model

import org.joda.time.Duration
import org.joda.time.LocalDate

data class DefaultContactDiaryLocationVisit(
    override val id: Long = 0L,
    override val date: LocalDate,
    override val contactDiaryLocation: ContactDiaryLocation,
    override val duration: Duration? = null,
    override val circumstances: String? = null
) : ContactDiaryLocationVisit

fun ContactDiaryLocationVisit.toEditableVariant(): DefaultContactDiaryLocationVisit {
    if (this is DefaultContactDiaryLocationVisit) return this
    return DefaultContactDiaryLocationVisit(
        id = id,
        date = date,
        contactDiaryLocation = contactDiaryLocation,
        duration = duration,
        circumstances = circumstances
    )
}
