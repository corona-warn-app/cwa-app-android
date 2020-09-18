package de.rki.coronawarnapp.http.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DistributionService {

    @GET
    suspend fun getDateIndex(@Url url: String): List<String>

    @GET
    suspend fun getHourIndex(@Url url: String): List<String>

    @Streaming
    @GET
    suspend fun getKeyFiles(@Url url: String): ResponseBody

    @GET
    suspend fun getApplicationConfiguration(@Url url: String): ResponseBody
}
