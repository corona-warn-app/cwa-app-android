package de.rki.coronawarnapp.util.files

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.rki.coronawarnapp.BuildConfig
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@Reusable
class FileSharing @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun getFileUri(path: File): Uri = FileProvider.getUriForFile(
        context,
        AUTHORITY,
        path
    )

    fun getFileIntentProvider(
        path: File,
        title: String,
        createChooserIntent: Boolean = false
    ): FileIntentProvider = object : FileIntentProvider {
        override fun intent(activity: Activity): Intent {
            val builder = ShareCompat.IntentBuilder(activity)
                .setType(path.determineMimeType())
                .setStream(getFileUri(path))
                .setSubject(title)

            val intent = if (createChooserIntent) {
                builder.createChooserIntent()
            } else {
                builder.intent
            }
            return intent.apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                Timber.tag(TAG).d("Intent created %s", this)
            }
        }

        override val file: File = path
    }

    interface FileIntentProvider {
        fun intent(activity: Activity): Intent
        val file: File
    }

    interface ShareIntentProvider {
        fun get(activity: Activity): Intent
    }

    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileProvider"
        private const val TAG = "FileSharing"
    }
}
