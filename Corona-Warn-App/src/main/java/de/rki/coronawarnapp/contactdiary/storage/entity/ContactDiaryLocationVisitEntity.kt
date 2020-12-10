package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.joda.time.LocalDate

@Entity
data class ContactDiaryLocationVisitEntity(
    @PrimaryKey val id: Long = 0L,
    val date: LocalDate,
    @ForeignKey(
        entity = ContactDiaryLocationEntity::class,
        parentColumns = ["locationId"],
        childColumns = ["fkLocationId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        deferred = true
    ) val fkLocationId: Long
)
