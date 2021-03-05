package de.rki.coronawarnapp.eventregistration.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.eventregistration.storage.dao.HostedEventDao
import de.rki.coronawarnapp.eventregistration.storage.entity.HostedEventEntity
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [
        HostedEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class EventRegistrationDatabase : RoomDatabase() {

    abstract fun hostedEventsDao(): HostedEventDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create(): EventRegistrationDatabase =
            Room.databaseBuilder(
                context,
                EventRegistrationDatabase::class.java,
                EVENT_REGISTRATION_DATABASE_NAME
            ).build()
    }

    companion object {
        private const val EVENT_REGISTRATION_DATABASE_NAME = "EventRegistration-db"
    }
}
