package de.rki.coronawarnapp.vaccination.core.server.valueset

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VaccinationValueSetApiV1 {

    @GET("/version/v1/ehn-dgc/{lang}/value-sets")
    suspend fun getValueSets(
        @Header("If-None-Match") etag: String = "",
        @Path("lang") languageCode: String
    ): Response<ResponseBody>
}
