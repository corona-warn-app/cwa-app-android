package de.rki.coronawarnapp.covidcertificate.signature.core.server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface DscApiV1 {

    @GET("/version/v1/ehn-dgc/dscs")
    suspend fun dscListCDN(): Response<ResponseBody>

    @GET("/trustList/DSC/")
    suspend fun dscList(): Response<ResponseBody>
}
