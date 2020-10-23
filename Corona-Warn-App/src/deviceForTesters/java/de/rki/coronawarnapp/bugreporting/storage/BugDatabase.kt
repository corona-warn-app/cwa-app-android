package de.rki.coronawarnapp.bugreporting.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.bugreporting.event.BugEventEntity
import de.rki.coronawarnapp.bugreporting.storage.dao.DefaultBugDao
import de.rki.coronawarnapp.bugreporting.util.Converters

@Database(
    entities = [BugEventEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BugDatabase : RoomDatabase() {

    abstract fun defaultBugDao(): DefaultBugDao

    companion object {
        private const val BUG_DATABASE_NAME = "bugreport-db"

        @Volatile private var instance: BugDatabase? = null

        fun getInstance(ctx: Context): BugDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(ctx)
                    .also { instance = it }
            }

        private fun buildDatabase(ctx: Context): BugDatabase = Room
            .databaseBuilder(ctx, BugDatabase::class.java, BUG_DATABASE_NAME)
            .build()
    }
}
