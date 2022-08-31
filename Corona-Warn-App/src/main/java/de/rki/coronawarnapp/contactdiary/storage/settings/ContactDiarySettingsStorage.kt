package de.rki.coronawarnapp.contactdiary.storage.settings

import androidx.datastore.core.DataStore
import dagger.Reusable
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import javax.inject.Inject

@Reusable
class ContactDiarySettingsStorage @Inject constructor(
    private val dataStore: DataStore<ContactDiarySettings>,
    private val serializer: ContactDiarySettingsSerializer
) : Resettable {

    val contactDiarySettings = dataStore.data.catch {
        Timber.tag(TAG).e(it, "Failed to get ContactDiarySettings")
        emit(serializer.defaultValue)
    }

    suspend fun updateContactDiarySettings(transform: suspend (t: ContactDiarySettings) -> ContactDiarySettings) {
        dataStore.updateData(transform)
    }

    override suspend fun reset() {
        Timber.tag(TAG).d("reset()")
        dataStore.updateData { serializer.defaultValue }
    }
}

private val TAG = tag<ContactDiarySettingsStorage>()
