package de.rki.coronawarnapp.contactdiary.storage.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "locations")
data class ContactDiaryLocationEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "locationId") override val locationId: Long = 0L,
    @ColumnInfo(name = "locationName") override var locationName: String
) : ContactDiaryLocation, Parcelable {
    override val stableId: Long
        get() = locationId
}

fun ContactDiaryLocation.toContactDiaryLocationEntity(): ContactDiaryLocationEntity =
    ContactDiaryLocationEntity(this.locationId, this.locationName)
