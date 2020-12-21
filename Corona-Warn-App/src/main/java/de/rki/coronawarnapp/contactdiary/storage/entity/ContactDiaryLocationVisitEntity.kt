package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import org.joda.time.LocalDate

@Entity(
    tableName = "locationvisits",
    foreignKeys = [
        ForeignKey(
            entity = ContactDiaryLocationEntity::class,
            parentColumns = ["locationId"],
            childColumns = ["fkLocationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        )
    ],
    indices = [Index("fkLocationId")]
)
data class ContactDiaryLocationVisitEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "fkLocationId") val fkLocationId: Long
)

fun ContactDiaryLocationVisit.toContactDiaryLocationVisitEntity(): ContactDiaryLocationVisitEntity =
    ContactDiaryLocationVisitEntity(id = this.id, date = this.date, fkLocationId = this.contactDiaryLocation.locationId)
