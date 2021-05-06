package de.rki.coronawarnapp.vaccination.core.server.valueset

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface VaccinationValueSetApiV1 {

    @GET("version/v1/ehn-dgc/value-sets/{lang}")
    suspend fun getValueSets(@Path("lang") languageCode: String): Response<ResponseBody>
}
