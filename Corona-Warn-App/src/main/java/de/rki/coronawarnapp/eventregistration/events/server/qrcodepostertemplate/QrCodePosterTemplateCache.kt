package de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrCodePosterTemplateCache @Inject constructor(@AppContext private val context: Context) {

    private val posterTemplateDir = File(context.filesDir, "events")

    fun getTemplate(): ByteArray {
        return File(posterTemplateDir, FILE_NAME).let { file ->
            if (file.exists()) {
                Timber.d("Loading template from $file")
                file.readBytes()
            } else {
                Timber.d("Loading default template")
                context.assets.open("default_qr_code_poster_template.pdf").readBytes()
            }
        }
    }

    fun saveTemplate(byteArray: ByteArray) {
        if (!posterTemplateDir.exists()) {
            posterTemplateDir.mkdirs()
        }

        File(posterTemplateDir, FILE_NAME).let { file ->
            Timber.d("Saving template to $file")
            file.writeBytes(byteArray)
        }
    }

    companion object {
        private const val FILE_NAME = "template.pdf"
    }
}
