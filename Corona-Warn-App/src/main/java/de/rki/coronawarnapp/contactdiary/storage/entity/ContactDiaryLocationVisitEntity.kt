package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import org.joda.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ContactDiaryLocationEntity::class,
            parentColumns = ["locationId"],
            childColumns = ["fkLocationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
data class ContactDiaryLocationVisitEntity(
    @PrimaryKey val id: Long = 0L,
    val date: LocalDate,
    val fkLocationId: Long
)

fun ContactDiaryLocationVisit.toContactDiaryLocationVisitEntity(): ContactDiaryLocationVisitEntity =
    ContactDiaryLocationVisitEntity(id = this.id, date = this.date, fkLocationId = this.contactDiaryLocation.locationId)
