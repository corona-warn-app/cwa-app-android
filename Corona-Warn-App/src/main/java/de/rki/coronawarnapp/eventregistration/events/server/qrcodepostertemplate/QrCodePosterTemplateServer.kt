package de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrCodePosterTemplateServer @Inject constructor(
    private val api: QrCodePosterTemplateApiV1
) {
    suspend fun retrieveQrCodePosterTemplate() {
        val response = api.getQrCodePosterTemplate()
        Timber.d("Received: %s", response)
    }
}
