package de.rki.coronawarnapp.coronatestjournal.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import javax.inject.Inject

@Suppress("MaxLineLength")
@Database(
    entities = [
        TestJournalEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class TestJournalDatabase : RoomDatabase() {

    abstract fun testResultDao(): TestJournalDao

    class Factory @Inject constructor(@AppContext private val context: Context) {

        fun create(databaseName: String = DATABASE_NAME): TestJournalDatabase {
            Timber.d("Instantiating test journal database.")
            return Room
                .databaseBuilder(context, TestJournalDatabase::class.java, databaseName)
                .build()
        }
    }

    companion object {
        private const val DATABASE_NAME = "test_journal.db"
    }
}
