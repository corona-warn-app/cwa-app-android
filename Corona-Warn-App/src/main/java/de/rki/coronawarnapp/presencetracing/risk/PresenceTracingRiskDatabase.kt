package de.rki.coronawarnapp.presencetracing.risk

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        TraceTimeIntervalMatchEntity::class,
        PresenceTracingRiskLevelResultEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RiskStateConverter::class)
abstract class PresenceTracingRiskDatabase : RoomDatabase() {

    abstract fun presenceTracingRiskLevelResultDao(): PresenceTracingRiskLevelResultDao
    abstract fun traceTimeIntervalMatchDao(): TraceTimeIntervalMatchDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create() = Room
            .databaseBuilder(context, PresenceTracingRiskDatabase::class.java, DATABASE_NAME)
            .build()
    }
}

private const val DATABASE_NAME = "PresenceTracingRisk_db"
