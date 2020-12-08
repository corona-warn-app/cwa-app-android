package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.Entity
import de.rki.coronawarnapp.contactdiary.model.Location

@Entity
data class LocationEntity(override var locationName: String) : Location
