package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import org.joda.time.LocalDate

@Entity(
    primaryKeys = ["fkDate", "fkPersonId"],
    foreignKeys = [
        ForeignKey(
            entity = ContactDiaryPersonEntity::class,
            parentColumns = ["personId"],
            childColumns = ["fkPersonId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContactDiaryDateEntity::class,
            parentColumns = ["date"],
            childColumns = ["fkDate"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class ContactDiaryElementPersonXRef(
    var fkDate: LocalDate,
    var fkPersonId: Long
)
