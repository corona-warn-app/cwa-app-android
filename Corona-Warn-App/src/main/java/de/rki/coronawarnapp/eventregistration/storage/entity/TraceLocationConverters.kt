package de.rki.coronawarnapp.eventregistration.storage.entity

import androidx.room.TypeConverter
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass

class TraceLocationConverters {

    @TypeConverter
    fun toTraceLocationType(value: Int): TraceLocationOuterClass.TraceLocationType =
        TraceLocationOuterClass.TraceLocationType.forNumber(value)!!

    @TypeConverter
    fun fromTraceLocationType(type: TraceLocationOuterClass.TraceLocationType): Int = type.number
}
