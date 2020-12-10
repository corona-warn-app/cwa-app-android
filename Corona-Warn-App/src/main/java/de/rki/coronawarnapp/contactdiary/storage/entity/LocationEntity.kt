package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.Location

@Entity
data class LocationEntity(override var locationName: String) : Location {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "location_id")
    var locationId: Long = 0L
}
