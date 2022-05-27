package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.util.trimToLength
import java.time.Duration
import java.time.LocalDate

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
    @ColumnInfo(name = "fkLocationId") val fkLocationId: Long,
    @ColumnInfo(name = "duration") val duration: Duration?,
    @ColumnInfo(name = "circumstances") val circumstances: String?,
    @ColumnInfo(name = "checkInID") val checkInID: Long?
)

fun ContactDiaryLocationVisit.toContactDiaryLocationVisitEntity(): ContactDiaryLocationVisitEntity =
    ContactDiaryLocationVisitEntity(
        id = this.id,
        date = this.date,
        fkLocationId = this.contactDiaryLocation.locationId,
        duration = this.duration,
        circumstances = this.circumstances?.trimToLength(250),
        checkInID = this.checkInID
    )
