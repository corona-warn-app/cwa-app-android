package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import org.joda.time.LocalDate

@Entity(primaryKeys = ["date", "personId"])
class ContactDiaryElementPersonXRef(
    var date: LocalDate,
    var personId: Long
)
