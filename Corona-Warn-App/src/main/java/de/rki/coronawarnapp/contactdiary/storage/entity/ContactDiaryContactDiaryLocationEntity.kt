package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation

@Entity
data class ContactDiaryContactDiaryLocationEntity(override var locationName: String): ContactDiaryLocation {
    @PrimaryKey(autoGenerate = true)
    var locationId: Long = 0L
}

fun ContactDiaryLocation.toContactDiaryContactDiaryLocationEntity(locationId: Long = 0L): ContactDiaryContactDiaryLocationEntity =
    when (this) {
        is ContactDiaryContactDiaryLocationEntity -> this
        else -> ContactDiaryContactDiaryLocationEntity(this.locationName)
            .apply { this.locationId = locationId }
    }
