package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import org.joda.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ContactDiaryPersonEntity::class,
            parentColumns = ["personId"],
            childColumns = ["fkPersonId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
data class ContactDiaryPersonEncounterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: LocalDate,
    val fkPersonId: Long
)

fun ContactDiaryPersonEncounter.toContactDiaryPersonEncounterEntity(): ContactDiaryPersonEncounterEntity =
    ContactDiaryPersonEncounterEntity(id = this.id, date = this.date, fkPersonId = this.contactDiaryPerson.personId)
