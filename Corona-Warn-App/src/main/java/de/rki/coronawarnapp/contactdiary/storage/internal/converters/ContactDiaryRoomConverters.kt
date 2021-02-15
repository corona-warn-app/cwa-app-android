package de.rki.coronawarnapp.contactdiary.storage.internal.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter

class ContactDiaryRoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun toContactDurationClassification(value: String?): ContactDiaryPersonEncounter.DurationClassification? {
        if (value == null) return null
        return ContactDiaryPersonEncounter.DurationClassification.values().singleOrNull { it.key == value }
    }

    @TypeConverter
    fun fromContactDurationClassification(value: ContactDiaryPersonEncounter.DurationClassification?): String? {
        return value?.key
    }
}
