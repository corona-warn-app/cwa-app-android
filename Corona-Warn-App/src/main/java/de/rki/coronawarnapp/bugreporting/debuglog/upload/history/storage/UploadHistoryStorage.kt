package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import androidx.datastore.core.DataStore
import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.UploadHistory
import de.rki.coronawarnapp.tag
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import javax.inject.Inject

@Reusable
class UploadHistoryStorage @Inject constructor(
    private val dataStore: DataStore<UploadHistory>
) {

    val uploadHistory = dataStore.data.catch {
        Timber.tag(TAG).e("Failed to get UploadHistory")
        emit(UploadHistory())
    }

    suspend fun update(transform: suspend (t: UploadHistory) -> UploadHistory) {
        dataStore.updateData(transform)
    }
}

private val TAG = tag<UploadHistoryStorage>()
