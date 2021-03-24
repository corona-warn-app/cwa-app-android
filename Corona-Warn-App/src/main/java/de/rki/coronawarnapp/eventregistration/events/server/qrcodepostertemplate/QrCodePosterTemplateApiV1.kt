package de.rki.coronawarnapp.eventregistration.events.server.qrcodepostertemplate

import de.rki.coronawarnapp.server.protocols.internal.evreg.PosterTemplate
import retrofit2.Response
import retrofit2.http.GET

interface QrCodePosterTemplateApiV1 {

    @GET("/version/v1/qr_code_poster_template_android")
    suspend fun getQrCodePosterTemplate(): Response<PosterTemplate.PosterTemplateAndroid>
}
