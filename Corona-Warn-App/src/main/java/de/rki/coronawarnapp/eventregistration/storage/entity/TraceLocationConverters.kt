package de.rki.coronawarnapp.eventregistration.storage.entity

import androidx.room.TypeConverter
import de.rki.coronawarnapp.eventregistration.events.TraceLocation

class TraceLocationConverters {

    @TypeConverter
    fun toTraceLocationType(value: Int) = enumValues<TraceLocation.Type>().single { it.value == value }

    @TypeConverter
    fun fromTraceLocationType(type: TraceLocation.Type) = type.value
}
