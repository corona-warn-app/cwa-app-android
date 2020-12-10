package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation

@Entity
data class ContactDiaryLocationEntity(override var locationName: String) : ContactDiaryLocation {
    @PrimaryKey(autoGenerate = true)
    var locationId: Long = 0L
}

fun ContactDiaryLocation.toContactDiaryLocationEntity(locationId: Long = 0L): ContactDiaryLocationEntity =
    when (this) {
        is ContactDiaryLocationEntity -> this
        else -> ContactDiaryLocationEntity(this.locationName)
            .apply { this.locationId = locationId }
    }
