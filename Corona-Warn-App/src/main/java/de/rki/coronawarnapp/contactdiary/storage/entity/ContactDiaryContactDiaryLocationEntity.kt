package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation

@Entity
data class ContactDiaryContactDiaryLocationEntity(override var locationName: String) : ContactDiaryLocation {
    @PrimaryKey(autoGenerate = true)
    var locationId: Long = 0L
}
