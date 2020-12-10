package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.joda.time.LocalDate

@Entity
data class ContactDiaryPersonEncounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: LocalDate,
    @ForeignKey(
        entity = ContactDiaryPersonEntity::class,
        parentColumns = ["personId"],
        childColumns = ["fkPersonId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        deferred = true
    ) val fkPersonId: Long
)
