package de.rki.coronawarnapp.eventregistration.checkins.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.eventregistration.checkins.storage.dao.EventCheckInDao
import de.rki.coronawarnapp.eventregistration.checkins.storage.entity.EventCheckInEntity
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        EventCheckInEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class EventDatabase : RoomDatabase() {

    abstract fun eventCheckInDao(): EventCheckInDao

    class Factory @Inject constructor(@AppContext private val ctx: Context) {
        fun create(databaseName: String = EVENT_DATABASE_NAME): EventDatabase = Room
            .databaseBuilder(ctx, EventDatabase::class.java, databaseName)
            .build()
    }

    companion object {
        private const val EVENT_DATABASE_NAME = "Event-db"
    }
}
