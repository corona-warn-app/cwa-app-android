package de.rki.coronawarnapp.presencetracing.locations.server.qrcodepostertemplate

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface QrCodePosterTemplateApiV1 {

    @GET("/version/v1/qr_code_poster_template_android")
    suspend fun getQrCodePosterTemplate(): Response<ResponseBody>
}
