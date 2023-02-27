package de.rki.coronawarnapp.util.database

import androidx.room.TypeConverter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import okio.ByteString.Companion.decodeBase64
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Suppress("TooManyFunctions")
class CommonConverters {
    private val mapper = ObjectMapper()

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return mapper.readValue(value)
    }

    @TypeConverter
    fun fromIntList(list: List<Int?>?): String {
        return mapper.writeValueAsString(list)
    }

    @TypeConverter
    fun toStringList(string: String?): List<String>? =
        string?.let { mapper.readValue(it) }

    @TypeConverter
    fun fromStringList(strings: List<String>?): String? =
        strings?.let { mapper.writeValueAsString(it) }

    @TypeConverter
    fun toUUID(value: String?): UUID? = value?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toPath(value: String?): File? = value?.let { File(it) }

    @TypeConverter
    fun fromPath(path: File?): String? = path?.path

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it) }

    @TypeConverter
    fun fromInstant(date: Instant?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromLocalTime(date: LocalTime?): String? = date?.toString()

    @TypeConverter
    fun toLocationCode(value: String?): LocationCode? = value?.let { LocationCode(it) }

    @TypeConverter
    fun fromLocationCode(code: LocationCode?): String? = code?.identifier

    @TypeConverter
    fun toTraceLocationId(value: String?): TraceLocationId? = value?.decodeBase64()

    @TypeConverter
    fun fromTraceLocationId(traceLocationId: TraceLocationId?): String? = traceLocationId?.base64()
}
