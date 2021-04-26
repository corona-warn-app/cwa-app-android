package de.rki.coronawarnapp.contactdiary.storage.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.util.trimToLength
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "locations")
data class ContactDiaryLocationEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "locationId") override val locationId: Long = 0L,
    @ColumnInfo(name = "locationName") override var locationName: String,
    override val phoneNumber: String?,
    override val emailAddress: String?,
    @ColumnInfo(name = "traceLocationID") override val traceLocationID: TraceLocationId?
) : ContactDiaryLocation, Parcelable {
    override val stableId: Long
        get() = locationId
}

private const val MAX_CHARACTERS = 250
private fun String.trimMaxCharacters(): String = this.trimToLength(MAX_CHARACTERS)

fun ContactDiaryLocation.toContactDiaryLocationEntity(): ContactDiaryLocationEntity =
    ContactDiaryLocationEntity(
        locationId = this.locationId,
        locationName = this.locationName.trimMaxCharacters(),
        phoneNumber = this.phoneNumber?.trimMaxCharacters(),
        emailAddress = this.emailAddress?.trimMaxCharacters(),
        traceLocationID = this.traceLocationID
    )
