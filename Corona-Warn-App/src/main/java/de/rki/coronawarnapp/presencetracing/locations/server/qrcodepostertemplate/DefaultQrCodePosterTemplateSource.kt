package de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate

import android.content.Context
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Reusable
class DefaultQrCodePosterTemplateSource @Inject constructor(@ApplicationContext private val context: Context) {

    fun getDefaultQrCodePosterTemplate() =
        context.assets.open("default_qr_code_poster_template_android.bin").readBytes()
}
