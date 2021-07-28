package de.rki.coronawarnapp.statistics.local.source

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LocalStatisticsApiV1 {
    @GET("/version/v1/local_stats_{id}")
    suspend fun getLocalStatistics(@Path("id") federalStateId: Int): Response<ResponseBody>
}
