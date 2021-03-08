package de.rki.coronawarnapp.eventregistration.checkins.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.eventregistration.checkins.storage.dao.TraceLocationCheckInDao
import de.rki.coronawarnapp.eventregistration.checkins.storage.entity.TraceLocationCheckInEntity
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        TraceLocationCheckInEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class TraceLocationDatabase : RoomDatabase() {

    abstract fun eventCheckInDao(): TraceLocationCheckInDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create(databaseName: String = EVENT_DATABASE_NAME): TraceLocationDatabase = Room
            .databaseBuilder(context, TraceLocationDatabase::class.java, databaseName)
            .build()
    }

    companion object {
        private const val EVENT_DATABASE_NAME = "TraceLocations_db"
    }
}
