package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.submission

import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiarySubmissionEntity
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataItem

data class SubmissionItem(
    val id: Long
) : DayDataItem {
    override val stableId: Long = id
}

fun ContactDiarySubmissionEntity?.toSubmissionItem(): SubmissionItem? = this?.let { SubmissionItem(id = id) }
