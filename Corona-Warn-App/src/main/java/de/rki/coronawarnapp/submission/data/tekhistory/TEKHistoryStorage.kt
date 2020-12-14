package de.rki.coronawarnapp.submission.data.tekhistory

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.submission.data.tekhistory.internal.TEKEntryDao
import de.rki.coronawarnapp.submission.data.tekhistory.internal.TEKHistoryDatabase
import de.rki.coronawarnapp.submission.data.tekhistory.internal.toPersistedTEK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TEKHistoryStorage @Inject constructor(
    private val tekHistoryDatabaseFactory: TEKHistoryDatabase.Factory
) {

    private val database by lazy { tekHistoryDatabaseFactory.create() }
    private val tekHistoryTables by lazy { database.tekHistory() }

    suspend fun storeTEKData(data: TEKBatch) = database.withTransaction {
        data.keys.forEach {
            val id = it.keyData.toByteString().base64()
            val newEntry = TEKEntryDao(
                id = id,
                batchId = data.batchId,
                obtainedAt = data.obtainedAt,
                persistedTEK = it.toPersistedTEK()
            )
            Timber.tag(TAG).v("Inserting TEK: %s", newEntry)
            try {
                tekHistoryTables.insertEntry(newEntry)
            } catch (e: SQLiteConstraintException) {
                Timber.i("TEK is already stored: %s", it)
            }
        }
    }

    val tekData: Flow<List<TEKBatch>> by lazy {
        tekHistoryTables.allEntries().map { tekDaos ->
            val batches = mutableMapOf<String, List<TEKEntryDao>>()
            tekDaos.forEach { tekDao ->
                batches[tekDao.batchId] = batches.getOrPut(tekDao.batchId) { emptyList() }.plus(tekDao)
            }
            batches.values.map { batchTEKs ->
                TEKBatch(
                    batchId = batchTEKs.first().batchId,
                    obtainedAt = batchTEKs.first().obtainedAt,
                    keys = batchTEKs.map { it.persistedTEK.toTemporaryExposureKey() }
                )
            }.toList()
        }
    }

    suspend fun clear() {
        Timber.w("clear() - Clearing all stored temporary exposure keys.")
        database.clearAllTables()
    }

    data class TEKBatch(
        val obtainedAt: Instant,
        val batchId: String,
        val keys: List<TemporaryExposureKey>
    )

    companion object {
        private const val TAG = "TEKHistoryStorage"
    }
}
