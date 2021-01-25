package de.rki.coronawarnapp.statistics.source

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

interface StatisticsApiV1 {

    @GET("/version/v1/stats")
    suspend fun getStatistics(): Response<ResponseBody>
}
