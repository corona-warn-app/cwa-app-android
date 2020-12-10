package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.LocalDate

@Entity
data class ContactDiaryDateEntity(@PrimaryKey val date: LocalDate)
