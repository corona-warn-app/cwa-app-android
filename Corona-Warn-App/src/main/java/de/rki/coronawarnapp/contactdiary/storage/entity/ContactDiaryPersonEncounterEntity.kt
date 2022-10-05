package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification
import de.rki.coronawarnapp.util.trimToLength
import java.time.LocalDate

@Entity(
    tableName = "personencounters",
    foreignKeys = [
        ForeignKey(
            entity = ContactDiaryPersonEntity::class,
            parentColumns = ["personId"],
            childColumns = ["fkPersonId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        )
    ],
    indices = [Index("fkPersonId")]
)
data class ContactDiaryPersonEncounterEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "fkPersonId") val fkPersonId: Long,
    @ColumnInfo(name = "durationClassification") val durationClassification: DurationClassification?,
    @ColumnInfo(name = "withMask") val withMask: Boolean?,
    @ColumnInfo(name = "wasOutside") val wasOutside: Boolean?,
    @ColumnInfo(name = "circumstances") val circumstances: String?
)

fun ContactDiaryPersonEncounter.toContactDiaryPersonEncounterEntity(): ContactDiaryPersonEncounterEntity =
    ContactDiaryPersonEncounterEntity(
        id = this.id,
        date = this.date,
        fkPersonId = this.contactDiaryPerson.personId,
        durationClassification = this.durationClassification,
        withMask = this.withMask,
        wasOutside = this.wasOutside,
        circumstances = this.circumstances?.trimToLength(250)
    )
