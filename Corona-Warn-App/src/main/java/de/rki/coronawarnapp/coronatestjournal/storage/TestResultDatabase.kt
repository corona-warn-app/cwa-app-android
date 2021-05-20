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
        TestResultEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class TestResultDatabase : RoomDatabase() {

    abstract fun testResultDao(): TestResultDao

    class Factory @Inject constructor(@AppContext private val context: Context) {

        fun create(databaseName: String = DATABASE_NAME): TestResultDatabase {
            Timber.d("Instantiating test result database.")
            return Room
                .databaseBuilder(context, TestResultDatabase::class.java, databaseName)
                .build()
        }
    }

    companion object {
        private const val DATABASE_NAME = "testresult.db"
    }
}
