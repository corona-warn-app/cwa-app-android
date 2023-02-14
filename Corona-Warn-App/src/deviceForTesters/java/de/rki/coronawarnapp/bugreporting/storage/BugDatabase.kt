package de.rki.coronawarnapp.bugreporting.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.bugreporting.event.BugEventEntity
import de.rki.coronawarnapp.bugreporting.storage.dao.DefaultBugDao
import de.rki.coronawarnapp.util.database.CommonConverters
import javax.inject.Inject

@Database(
    entities = [BugEventEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(CommonConverters::class)
abstract class BugDatabase : RoomDatabase() {

    abstract fun defaultBugDao(): DefaultBugDao

    class Factory @Inject constructor(@ApplicationContext private val context: Context) {
        fun create(): BugDatabase = Room
            .databaseBuilder(context, BugDatabase::class.java, BUG_DATABASE_NAME)
            .build()
    }

    companion object {
        private const val BUG_DATABASE_NAME = "bugreport-db"
    }
}
