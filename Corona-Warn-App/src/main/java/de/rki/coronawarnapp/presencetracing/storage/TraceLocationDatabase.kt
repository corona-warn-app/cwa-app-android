package de.rki.coronawarnapp.presencetracing.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.presencetracing.storage.dao.CheckInDao
import de.rki.coronawarnapp.presencetracing.storage.dao.TraceLocationDao
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationConverters
import de.rki.coronawarnapp.presencetracing.storage.entity.TraceLocationEntity
import de.rki.coronawarnapp.presencetracing.storage.migration.PresenceTracingDatabaseMigration1To2
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        TraceLocationCheckInEntity::class,
        TraceLocationEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(CommonConverters::class, TraceLocationConverters::class)
abstract class TraceLocationDatabase : RoomDatabase() {

    abstract fun checkInDao(): CheckInDao
    abstract fun traceLocationDao(): TraceLocationDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create(databaseName: String = TRACE_LOCATIONS_DATABASE_NAME): TraceLocationDatabase = Room
            .databaseBuilder(context, TraceLocationDatabase::class.java, databaseName)
            .addMigrations(PresenceTracingDatabaseMigration1To2)
            .build()
    }
}

private const val TRACE_LOCATIONS_DATABASE_NAME = "TraceLocations_db"
