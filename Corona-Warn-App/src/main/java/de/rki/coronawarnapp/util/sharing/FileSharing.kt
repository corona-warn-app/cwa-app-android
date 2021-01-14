package de.rki.coronawarnapp.util.sharing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.app.ShareCompat
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

    fun getIntentProvider(
        path: File,
        title: String,
        @StringRes chooserTitle: Int? = null
    ): ShareIntentProvider = object : ShareIntentProvider {
        override fun get(activity: Activity): Intent {
            val builder = ShareCompat.IntentBuilder.from(activity).apply {
                setType(determineMimeType(path))
                setStream(getFileUri(path))
                setSubject(title)
                chooserTitle?.let { setChooserTitle(it) }
            }

            val intent = if (chooserTitle != null) {
                builder.createChooserIntent()
            } else {
                builder.intent
            }
            return intent.apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                Timber.tag(TAG).d("Intent created %s", this)
            }
        }
    }

    interface ShareIntentProvider {
        fun get(activity: Activity): Intent
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
