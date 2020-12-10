package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import org.joda.time.LocalDate

@Entity(
    primaryKeys = ["fkDate", "fkLocationId"],
    foreignKeys = [
        ForeignKey(
            entity = ContactDiaryLocationEntity::class,
            parentColumns = ["locationId"],
            childColumns = ["fkLocationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContactDiaryDateEntity::class,
            parentColumns = ["date"],
            childColumns = ["fkDate"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactDiaryElementLocationXRef(
    var fkDate: LocalDate,
    var fkLocationId: Long
)
