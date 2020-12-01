package de.rki.coronawarnapp.submission.data.tekhistory

import android.app.Activity
import android.content.Intent
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class TEKHistoryUpdater @Inject constructor(
    private val tekHistoryStorage: TEKHistoryStorage,
    private val timeStamper: TimeStamper,
    private val enfClient: ENFClient,
    @AppScope private val scope: CoroutineScope
) {

    suspend fun updateTEKHistoryOrRequestPermission(hostActivity: Activity) {
        enfClient.getTEKHistoryOrRequestPermission(
            onTEKHistoryAvailable = { teks ->
                scope.launch { updateTEKHistory() }
            },
            onPermissionRequired = { status ->
                status.startResolutionForResult(hostActivity, TEK_PERMISSION_REQUESTCODE)
            }
        )
    }

    suspend fun updateHistoryOrThrow() {
        val async = scope.async {
            updateTEKHistory()
        }
        async.await()
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): UpdateResult {
        if (requestCode != TEK_PERMISSION_REQUESTCODE) {
            Timber.tag(TAG).w("Not our request code ($requestCode): %s", data)
            return UpdateResult.UNKNOWN_RESULT
        }
        if (resultCode != Activity.RESULT_OK) {
            Timber.tag(TAG).w("Negative activity result (!= RESULT_OK): %s", data)
            return UpdateResult.NO_PERMISSION
        }

        return UpdateResult.PERMISSION_AVAILABLE
    }

    private suspend fun updateTEKHistory() {
        val tekBatch = TEKHistoryStorage.TEKBatch(
            batchId = UUID.randomUUID().toString(),
            obtainedAt = timeStamper.nowUTC,
            keys = enfClient.getTEKHistory()
        )

        Timber.i("Permission are available, storing TEK history.")
        tekHistoryStorage.storeTEKData(tekBatch)
    }

    enum class UpdateResult {
        PERMISSION_AVAILABLE,
        NO_PERMISSION,
        UNKNOWN_RESULT
    }

    companion object {
        private const val TAG = "TEKHistoryUpdater"
        const val TEK_PERMISSION_REQUESTCODE = 3011
    }
}
