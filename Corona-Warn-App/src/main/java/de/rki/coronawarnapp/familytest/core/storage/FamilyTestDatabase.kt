package de.rki.coronawarnapp.familytest.core.storage

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@Database(
    entities = [FamilyCoronaTestEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(FamilyCoronaTestConverter::class)
abstract class FamilyTestDatabase : RoomDatabase() {

    abstract fun familyCoronaTestDao(): FamilyCoronaTestDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create(): FamilyTestDatabase = Room
            .databaseBuilder(context, FamilyTestDatabase::class.java, DATABASE_NAME)
            .build()
    }

    companion object {
        private const val DATABASE_NAME = "FamilyTest-db"
    }
}

class FamilyCoronaTestConverter {

    private val gson: Gson = SerializationModule.baseGson

    @TypeConverter
    fun toFamilyCoronaTest(value: String): FamilyCoronaTest? = try {
        gson.fromJson(value)
    } catch (e: Exception) {
        Timber.e(e, "Can't create FamilyCoronaTest from value=%s", value)
        null
    }

    @TypeConverter
    fun fromFamilyCoronaTest(test: FamilyCoronaTest): String {
        return gson.toJson(test)
    }
}

@Entity(tableName = "family_corona_test")
data class FamilyCoronaTestEntity(
    @PrimaryKey
    val identifier: TestIdentifier,
    @ColumnInfo(name = "test")
    val test: FamilyCoronaTest,
    @ColumnInfo(name = "moved_to_recycle_bin_at_millis")
    val movedToRecycleBinAtMillis: Long? = null,
)

@Dao
interface FamilyCoronaTestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FamilyCoronaTestEntity)

    @Delete
    suspend fun delete(entity: FamilyCoronaTestEntity)

    @Query("SELECT * FROM family_corona_test WHERE moved_to_recycle_bin_at_millis IS NULL")
    fun getAllActive(): Flow<List<FamilyCoronaTestEntity?>>

    @Query("SELECT * FROM family_corona_test WHERE moved_to_recycle_bin_at_millis IS NOT NULL")
    fun getAllInRecycleBin(): Flow<List<FamilyCoronaTestEntity?>>

    @Transaction
    @Query("DELETE FROM family_corona_test")
    suspend fun deleteAll()

    @Transaction
    @Query("UPDATE family_corona_test SET moved_to_recycle_bin_at_millis = :atMillis WHERE identifier IN(:ids)")
    suspend fun moveAllToRecycleBin(ids: List<TestIdentifier>, atMillis: Long)

    @Query("SELECT * FROM family_corona_test WHERE identifier = :identifier")
    suspend fun get(identifier: TestIdentifier): FamilyCoronaTestEntity?

    @Transaction
    suspend fun update(identifier: TestIdentifier, update: (FamilyCoronaTest) -> FamilyCoronaTest) {
        get(identifier)?.let {
            val updated = update(it.test).toEntity()
            if (it != updated) insert(updated)
        }
    }

    @Transaction
    suspend fun update(updates: List<Pair<TestIdentifier, (FamilyCoronaTest) -> FamilyCoronaTest>>) {
        updates.forEach {
            get(it.first)?.let { test ->
                val updated = it.second(test.test).toEntity()
                if (test != updated) insert(updated)
            }
        }
    }
}
