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
import com.google.gson.GsonBuilder
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.adapter.InstantAdapter
import de.rki.coronawarnapp.util.serialization.fromJson
import kotlinx.coroutines.flow.Flow
import org.joda.time.Instant
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

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantAdapter())
        .create()

    @TypeConverter
    fun toFamilyCoronaTest(value: String): FamilyCoronaTest? = try {
        gson.fromJson(value)
    } catch (e: Exception) {
        Timber.e(e, "Can't create FamilyCoronaTest")
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

    @Query("SELECT * FROM family_corona_test WHERE moved_to_recycle_bin_at_millis = NULL")
    fun getAll(): Flow<List<FamilyCoronaTestEntity>>

    @Query("SELECT * FROM family_corona_test WHERE moved_to_recycle_bin_at_millis != NULL")
    fun getAllInRecycleBin(): Flow<List<FamilyCoronaTestEntity>>

    @Query("DELETE FROM family_corona_test WHERE moved_to_recycle_bin_at_millis < :olderThanMillis")
    suspend fun deleteFromRecycleBin(olderThanMillis: Long)

    @Query("DELETE FROM family_corona_test")
    suspend fun deleteAll()

    @Query("SELECT * FROM family_corona_test WHERE identifier = :identifier")
    fun get(identifier: TestIdentifier): FamilyCoronaTestEntity?

    @Transaction
    suspend fun update(identifier: TestIdentifier, update: (FamilyCoronaTest) -> FamilyCoronaTest) {
        get(identifier)?.let {
            insert(update(it.test).toEntity())
        }
    }
}
