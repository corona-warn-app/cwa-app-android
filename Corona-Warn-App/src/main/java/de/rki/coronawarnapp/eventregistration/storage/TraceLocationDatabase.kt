package de.rki.coronawarnapp.eventregistration.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.eventregistration.storage.dao.CheckInDao
import de.rki.coronawarnapp.eventregistration.storage.dao.TraceLocationDao
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationConverters
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.presencetracing.risk.TraceTimeIntervalMatchDao
import de.rki.coronawarnapp.presencetracing.risk.TraceTimeIntervalMatchEntity
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        TraceLocationCheckInEntity::class,
        TraceLocationEntity::class,
        TraceTimeIntervalMatchEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class, TraceLocationConverters::class)
abstract class TraceLocationDatabase : RoomDatabase() {

    abstract fun eventCheckInDao(): CheckInDao
    abstract fun traceLocationDao(): TraceLocationDao
    abstract fun traceTimeIntervalMatchDao(): TraceTimeIntervalMatchDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create() = Room
            .databaseBuilder(context, TraceLocationDatabase::class.java, TRACE_LOCATIONS_DATABASE_NAME)
            .build()
    }
}

private const val TRACE_LOCATIONS_DATABASE_NAME = "TraceLocations_db"
