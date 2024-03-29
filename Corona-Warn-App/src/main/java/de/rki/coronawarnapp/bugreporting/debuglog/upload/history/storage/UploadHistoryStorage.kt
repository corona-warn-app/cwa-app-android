package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import androidx.datastore.core.DataStore
import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.UploadHistory
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import javax.inject.Inject

@Reusable
class UploadHistoryStorage @Inject constructor(
    private val dataStore: DataStore<UploadHistory>
) : Resettable {

    val uploadHistory = dataStore.data.catch {
        Timber.tag(TAG).e("Failed to get UploadHistory")
        emit(UploadHistory())
    }

    suspend fun update(transform: suspend (t: UploadHistory) -> UploadHistory) {
        dataStore.updateData(transform)
    }

    override suspend fun reset() {
        Timber.tag(TAG).d("reset")
        dataStore.updateData { UploadHistory() }
    }
}

private val TAG = tag<UploadHistoryStorage>()
