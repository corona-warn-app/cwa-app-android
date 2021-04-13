package de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Reusable
class DefaultQrCodePosterTemplateSource @Inject constructor(@AppContext private val context: Context) {

    fun getDefaultQrCodePosterTemplate() =
        context.assets.open("default_qr_code_poster_template_android.bin").readBytes()
}
