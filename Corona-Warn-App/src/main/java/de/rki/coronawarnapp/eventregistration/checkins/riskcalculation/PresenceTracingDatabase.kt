package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationConverters
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        TraceTimeIntervalMatchEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class, TraceLocationConverters::class)
abstract class PresenceTracingDatabase : RoomDatabase() {

    abstract fun traceTimeIntervalMatchDao(): TraceTimeIntervalMatchDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create() = Room
            .databaseBuilder(context, PresenceTracingDatabase::class.java, PRESENCE_TRACING_DATABASE_NAME)
            .build()
    }
}

private const val PRESENCE_TRACING_DATABASE_NAME = "PresenceTracing_db"
