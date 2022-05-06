package de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.DccTicketing
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@Reusable
class DccTicketingAllowListStorage @Inject constructor(
    @DccTicketing private val localStorage: File
) : Resettable {

    private val mutex = Mutex()
    private val allowListLocalData = File(localStorage, ALLOW_LIST_FILE_NAME)

    suspend fun load(): ByteArray? = mutex.withLock {
        Timber.tag(TAG).v("Loading data")
        allowListLocalData.load()
    }

    suspend fun save(data: ByteArray) = mutex.withLock {
        Timber.tag(TAG).v("Saving data")
        allowListLocalData.save(data = data)
    }

    private fun File.load(): ByteArray? = try {
        when (exists()) {
            true -> readBytes()
            false -> {
                Timber.tag(TAG).v("%s does not exist", name)
                null
            }
        }
    } catch (e: Exception) {
        Timber.tag(TAG).w(e, "Failed to load data from %s. Returning null.", name)
        null
    }

    private fun File.save(data: ByteArray) = try {
        if (exists()) {
            Timber.tag(TAG).v("Replacing %s with new data", name)
        }
        parentFile?.mkdirs()
        writeBytes(data)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Saving data failed.")
    }

    override suspend fun reset() = mutex.withLock {
        localStorage.reset()
    }

    private fun File.reset() {
        if (!exists()) {
            Timber.tag(TAG).d("%s did not exist, so nothing to delete", name)
            return
        }

        deleteRecursively().also { Timber.tag(TAG).d("Deleted %s successfully %b", name, it) }
    }

    companion object {
        private val TAG = tag<DccTicketingAllowListStorage>()

        private const val ALLOW_LIST_FILE_NAME = "dcc_ticketing_allow_list_raw"
    }
}
