package de.rki.coronawarnapp.diagnosiskeys.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.util.HashExtensions.toSHA1
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "keyfiles")
data class CachedKeyInfo(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "type") val type: Type,
    @ColumnInfo(name = "location") val location: LocationCode, // i.e. "DE"
    @ColumnInfo(name = "day") val day: LocalDate, // i.e. 2020-08-23
    @ColumnInfo(name = "hour") val hour: LocalTime?, // i.e. 23
    @ColumnInfo(name = "createdAt") val createdAt: Instant,
    @ColumnInfo(name = "checksumMD5") val etag: String?, // ETag
    @ColumnInfo(name = "completed") val isDownloadComplete: Boolean,
    @ColumnInfo(name = "checkedForExposures") val checkedForExposures: Boolean = false
) {

    constructor(
        type: Type,
        location: LocationCode,
        day: LocalDate,
        hour: LocalTime?,
        createdAt: Instant,
    ) : this(
        id = calculateId(location, day, hour, type),
        location = location,
        day = day,
        hour = hour,
        type = type,
        createdAt = createdAt,
        etag = null,
        isDownloadComplete = false
    )

    val fileName: String
        get() = "$id.zip"

    fun toDownloadUpdate(etag: String): DownloadUpdate = DownloadUpdate(
        id = id,
        etag = etag,
        isDownloadComplete = true
    )

    companion object {
        fun calculateId(
            location: LocationCode,
            day: LocalDate,
            hour: LocalTime?,
            type: Type
        ): String {
            var rawId = "${location.identifier}.${type.typeValue}.$day"
            hour?.let { rawId += ".${hour.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))}" }
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

val CachedKeyInfo.pkgDateTime: ZonedDateTime
    get() = when (type) {
        CachedKeyInfo.Type.LOCATION_DAY -> day.atStartOfDay(ZoneOffset.UTC)
        CachedKeyInfo.Type.LOCATION_HOUR -> day.atTime(hour).atZone(ZoneOffset.UTC)
    }

val CachedKeyInfo.sortDateTime: ZonedDateTime
    get() = when (type) {
        CachedKeyInfo.Type.LOCATION_DAY -> day.atTime(endOfDay).atZone(ZoneOffset.UTC)
        CachedKeyInfo.Type.LOCATION_HOUR -> day.atTime(hour).atZone(ZoneOffset.UTC)
    }

// use end of day to ensure correct order of packages when day and hour packages are mixed
private val endOfDay: LocalTime
    get() = LocalTime.of(23, 59, 59)
