package de.rki.coronawarnapp.covidcertificate.valueset.server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CertificateValueSetApiV1 {

    @GET("/version/v1/ehn-dgc/{lang}/value-sets")
    suspend fun getValueSets(@Path("lang") languageCode: String): Response<ResponseBody>
}
