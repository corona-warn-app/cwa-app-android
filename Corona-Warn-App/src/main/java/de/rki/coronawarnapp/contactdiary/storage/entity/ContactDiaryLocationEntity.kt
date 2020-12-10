package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation

@Entity
data class ContactDiaryLocationEntity(
    @PrimaryKey(autoGenerate = true) override val locationId: Long = 0L,
    override var locationName: String
) : ContactDiaryLocation

fun ContactDiaryLocation.toContactDiaryLocationEntity(): ContactDiaryLocationEntity =
    ContactDiaryLocationEntity(this.locationId, this.locationName)
