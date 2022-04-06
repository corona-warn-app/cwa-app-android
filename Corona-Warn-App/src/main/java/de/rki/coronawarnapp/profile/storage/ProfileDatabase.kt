package de.rki.coronawarnapp.profile.storage

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import de.rki.coronawarnapp.util.di.AppContext
import org.joda.time.LocalDate
import javax.inject.Inject

@Database(
    entities = [ProfileEntity::class],
    version = 1,
    exportSchema = true
)

abstract class ProfileDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create(): ProfileDatabase = Room
            .databaseBuilder(context, ProfileDatabase::class.java, DATABASE_NAME)
            .build()
    }

    companion object {
        private const val DATABASE_NAME = "Profile-db"
    }
}

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate?,
    val street: String,
    val zipCode: String,
    val city: String,
    val phone: String,
    val email: String
)

@Dao
interface ProfileDao
