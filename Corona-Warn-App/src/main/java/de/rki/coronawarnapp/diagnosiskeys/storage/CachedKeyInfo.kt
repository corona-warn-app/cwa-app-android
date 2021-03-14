package de.rki.coronawarnapp.diagnosiskeys.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.HashExtensions.toSHA1
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime

@Entity(tableName = "keyfiles")
data class CachedKeyInfo(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "type") val type: Type,
    @ColumnInfo(name = "location") val location: LocationCode, // i.e. "DE"
    @ColumnInfo(name = "day") val day: LocalDate, // i.e. 2020-08-23
    @ColumnInfo(name = "hour") val hour: LocalTime?, // i.e. 23
    @ColumnInfo(name = "createdAt") val createdAt: Instant,
    @ColumnInfo(name = "checksumMD5") val etag: String?, // ETag
    @ColumnInfo(name = "completed") val isDownloadComplete: Boolean
) {

    constructor(
        type: Type,
        location: LocationCode,
        day: LocalDate,
        hour: LocalTime?,
        createdAt: Instant
    ) : this(
        id = calcluateId(location, day, hour, type),
        location = location,
        day = day,
        hour = hour,
        type = type,
        createdAt = createdAt,
        etag = null,
        isDownloadComplete = false
    )

    @Transient
    val fileName: String = "$id.zip"

    fun toDownloadUpdate(etag: String): DownloadUpdate = DownloadUpdate(
        id = id,
        etag = etag,
        isDownloadComplete = true
    )

    fun toDateTime(): DateTime = when (type) {
        Type.LOCATION_DAY -> day.toDateTimeAtStartOfDay(DateTimeZone.UTC)
        Type.LOCATION_HOUR -> day.toDateTime(hour, DateTimeZone.UTC)
    }

    companion object {
        fun calcluateId(
            location: LocationCode,
            day: LocalDate,
            hour: LocalTime?,
            type: Type
        ): String {
            var rawId = "${location.identifier}.${type.typeValue}.$day"
            hour?.let { rawId += ".$hour" }
            return rawId.toSHA1()
        }
    }

    enum class Type constructor(internal val typeValue: String) {
        LOCATION_DAY("country_day"),
        LOCATION_HOUR("country_hour");

        class Converter {
            @TypeConverter
            fun toType(value: String?): Type? =
                value?.let { values().single { it.typeValue == value } }

            @TypeConverter
            fun fromType(type: Type?): String? = type?.typeValue
        }
    }

    @Entity
    data class DownloadUpdate(
        @PrimaryKey @ColumnInfo(name = "id") val id: String,
        @ColumnInfo(name = "checksumMD5") val etag: String?,
        @ColumnInfo(name = "completed") val isDownloadComplete: Boolean
    )
}
