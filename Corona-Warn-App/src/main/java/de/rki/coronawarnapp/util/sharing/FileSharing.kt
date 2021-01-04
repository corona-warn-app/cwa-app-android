package de.rki.coronawarnapp.util.sharing

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@Reusable
class FileSharing @Inject constructor(
    @AppContext private val context: Context
) {

    private fun getFileUri(path: File): Uri = FileProvider.getUriForFile(
        context,
        AUTHORITY,
        path
    )

    fun getIntent(
        path: File,
        title: String,
        body: String? = null
    ): Intent = Intent(Intent.ACTION_SEND).apply {
        type = determineMimeType(path)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, getFileUri(path))

        putExtra(Intent.EXTRA_SUBJECT, title)
        body?.let { putExtra(Intent.EXTRA_TEXT, it) }

        Timber.tag(TAG).d("Intent created %s", this)
    }

    private fun determineMimeType(path: File): String = when {
        path.name.endsWith(".zip") -> "application/zip"
        else -> throw UnsupportedOperationException("Unsupported MIME type: $path")
    }

    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileProvider"
        private const val TAG = "FileSharing"
    }
}
