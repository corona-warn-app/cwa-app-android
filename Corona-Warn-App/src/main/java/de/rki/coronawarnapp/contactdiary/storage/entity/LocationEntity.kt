package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.Location

@Entity
data class LocationEntity(override var locationName: String): Location {
    @PrimaryKey(autoGenerate = true)
    var locationId: Long = 0L
}
