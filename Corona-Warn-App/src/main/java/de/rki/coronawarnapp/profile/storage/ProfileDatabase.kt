package de.rki.coronawarnapp.profile.storage

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettingsDataStore
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.database.CommonConverters
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
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
        private val settings: RATProfileSettingsDataStore,
        ) {
        fun create(): ProfileDatabase = Room
            .databaseBuilder(context, ProfileDatabase::class.java, DATABASE_NAME)
            .addCallback(object: RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch {
                        migrateFromDataStore(db)
                    }
                }
            })
            .build()

        private suspend fun migrateFromDataStore(db: SupportSQLiteDatabase) {
            val ratProfile = settings.profileFlow.first()
            if (ratProfile != null) {
                val values = ContentValues().apply {
                    put("first_name", ratProfile.firstName)
                    put("last_name", ratProfile.lastName)
                    put("birth_date", ratProfile.birthDate?.toString())
                    put("street", ratProfile.street)
                    put("zip_code", ratProfile.zipCode)
                    put("city", ratProfile.city)
                    put("phone", ratProfile.phone)
                    put("email", ratProfile.email)
                }

                with(db) {
                    beginTransaction()
                    insert("profile", SQLiteDatabase.CONFLICT_ABORT, values)
                    setTransactionSuccessful()
                    endTransaction()
                }

                settings.deleteProfile()
            }

        }
    }

    companion object {
        private const val DATABASE_NAME = "Profile-db"
    }
}

@Entity(tableName = "profile")
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
    suspend fun insert(entity: ProfileEntity)

    @Transaction
    @Query("DELETE FROM profile WHERE id = :id")
    suspend fun delete(id: Int)

    @Update
    suspend fun update(entity: ProfileEntity)

    @Transaction
    @Query("SELECT * FROM profile")
    fun getAll(): Flow<List<ProfileEntity>>
}
