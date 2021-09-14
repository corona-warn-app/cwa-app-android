package de.rki.coronawarnapp.contactdiary.storage.internal.converters

import androidx.room.TypeConverter
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import org.joda.time.Duration

class ContactDiaryRoomConverters {
    @TypeConverter
    fun toContactDurationClassification(value: String?): DurationClassification? =
        when (value) {
            // Map old values
            "LessThan15Minutes" -> DurationClassification.LESS_THAN_10_MINUTES
            "MoreThan15Minutes" -> DurationClassification.MORE_THAN_10_MINUTES
            else -> DurationClassification.values().singleOrNull { it.key == value }
        }

    @TypeConverter
    fun fromContactDurationClassification(value: DurationClassification?): String? {
        return value?.key
    }

    @TypeConverter
    fun toJodaDuration(millis: Long?): Duration? {
        return millis?.let { Duration.millis(it) }
    }

    @TypeConverter
    fun fromJodaDuration(duration: Duration?): Long? {
        return duration?.millis
    }

    @TypeConverter
    fun toTestType(value: String?): ContactDiaryCoronaTestEntity.TestType? =
        ContactDiaryCoronaTestEntity.TestType.values().singleOrNull { it.raw == value }

    @TypeConverter
    fun fromTestType(type: ContactDiaryCoronaTestEntity.TestType?): String? = type?.raw

    @TypeConverter
    fun toTestResult(value: String?): ContactDiaryCoronaTestEntity.TestResult? =
        ContactDiaryCoronaTestEntity.TestResult.values().singleOrNull { it.raw == value }

    @TypeConverter
    fun fromTestResult(type: ContactDiaryCoronaTestEntity.TestResult?): String? = type?.raw
}
