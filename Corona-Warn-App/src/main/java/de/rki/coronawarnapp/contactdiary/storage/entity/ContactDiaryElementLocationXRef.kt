package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import org.joda.time.LocalDate

@Entity(primaryKeys = ["date", "locationId"])
class ContactDiaryElementLocationXRef(
    var date: LocalDate,
    var locationId: Long
)
