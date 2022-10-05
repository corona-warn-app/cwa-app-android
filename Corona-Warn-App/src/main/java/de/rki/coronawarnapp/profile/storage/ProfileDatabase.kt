package de.rki.coronawarnapp.profile.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import de.rki.coronawarnapp.profile.legacy.RATProfile
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@Database(
    entities = [ProfileEntity::class],
    version = 1,
    exportSchema = true
)

@TypeConverters(CommonConverters::class)
abstract class ProfileDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

    class Factory @Inject constructor(
        @AppContext private val context: Context,
        @AppScope private val scope: CoroutineScope,
        private val settings: ProfileSettingsDataStore,
    ) {
        fun create(): ProfileDatabase = Room
            .databaseBuilder(context, ProfileDatabase::class.java, PROFILE_DATABASE_NAME)
            .addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch {
                        migrateFromDataStore(db)
                    }
                }
            })
            .build()

        @VisibleForTesting
        internal suspend fun migrateFromDataStore(db: SupportSQLiteDatabase) {
            Timber.i("Start migration of profile")
            val ratProfile = settings.profileFlow.first()
            if (ratProfile != null) {
                Timber.d("Migrate profile data to database")
                val values = ratProfile.toContentValues()
                with(db) {
                    beginTransaction()
                    insert(PROFILE_TABLE_NAME, SQLiteDatabase.CONFLICT_ABORT, values).also {
                        Timber.d("Inserted into db with id $it")
                    }
                    setTransactionSuccessful()
                    endTransaction()
                }
                @Suppress("DEPRECATION")
                settings.deleteProfile()
            }
            Timber.i("Migration complete")
        }
    }
}

internal const val PROFILE_DATABASE_NAME = "Profile-db"
internal const val PROFILE_TABLE_NAME = "profile"

@Entity(tableName = PROFILE_TABLE_NAME)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "last_name")
    val lastName: String,

    @ColumnInfo(name = "birth_date")
    val birthDate: LocalDate?,

    @ColumnInfo(name = "street")
    val street: String,

    @ColumnInfo(name = "zip_code")
    val zipCode: String,

    @ColumnInfo(name = "city")
    val city: String,

    @ColumnInfo(name = "phone")
    val phone: String,

    @ColumnInfo(name = "email")
    val email: String
)

@Dao
interface ProfileDao {
    @Insert
    suspend fun insert(entity: ProfileEntity): Long

    @Transaction
    @Query("DELETE FROM $PROFILE_TABLE_NAME WHERE id = :id")
    suspend fun delete(id: Int)

    @Transaction
    @Query("DELETE FROM $PROFILE_TABLE_NAME")
    suspend fun deleteAll()

    @Update
    suspend fun update(entity: ProfileEntity)

    @Transaction
    @Query("SELECT * FROM $PROFILE_TABLE_NAME")
    fun getAll(): Flow<List<ProfileEntity>>
}

internal fun RATProfile.toContentValues(): ContentValues = ContentValues().apply {
    put("first_name", firstName)
    put("last_name", lastName)
    put("birth_date", birthDate?.toString())
    put("street", street)
    put("zip_code", zipCode)
    put("city", city)
    put("phone", phone)
    put("email", email)
}
