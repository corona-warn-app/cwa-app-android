package de.rki.coronawarnapp.bugreporting.debuglog.export

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import de.rki.coronawarnapp.bugreporting.debuglog.internal.LogSnapshotter
import de.rki.coronawarnapp.util.files.determineMimeType
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SAFLogExport @Inject constructor() {
    private var lastId = 1
    private val requestMap = mutableMapOf<Int, Request>()

    fun createSAFRequest(snapshot: LogSnapshotter.Snapshot): Request {
        val request = Request(
            id = ++lastId,
            snapshot = snapshot
        )
        requestMap[request.id] = request
        return request
    }

    fun getRequest(id: Int): Request? = requestMap[id]

    data class Request(
        val id: Int,
        val snapshot: LogSnapshotter.Snapshot,
    ) {
        fun createIntent() = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = snapshot.path.determineMimeType()
            putExtra(Intent.EXTRA_TITLE, snapshot.path.name)
        }

        fun storeSnapshot(resolver: ContentResolver, uri: Uri): Result {
            Timber.tag(TAG).d("Writing to %s", uri)
            resolver.openOutputStream(uri)!!.sink().buffer().use { dest ->
                snapshot.path.source().buffer().use { source ->
                    dest.writeAll(source)
                }
            }
            Timber.tag(TAG).i("%s was written to %s", snapshot, uri)

            snapshot.delete().also {
                Timber.tag(TAG).d("Snapshot deleted: %s", snapshot)
            }
            return Result(uri)
        }

        data class Result(val storageUri: Uri)
    }

    companion object {
        private const val TAG = "SAFLogSharing"
    }
}
