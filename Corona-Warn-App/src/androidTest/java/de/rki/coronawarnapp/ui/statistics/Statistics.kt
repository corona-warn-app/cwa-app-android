package de.rki.coronawarnapp.ui.statistics

import android.content.Context
import android.content.SharedPreferences
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.environment.download.DownloadCDNModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.StatisticsModule
import de.rki.coronawarnapp.statistics.source.StatisticsParser
import de.rki.coronawarnapp.statistics.source.StatisticsServer
import de.rki.coronawarnapp.util.security.VerificationKeys
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

object Statistics {

    private fun loadRealStatisticsData(): StatisticsData? {
        val context = mockk<Context>(relaxed = true)
        val preferences = mockk<SharedPreferences>(relaxed = true)
        val cache = mockk<Cache>(relaxed = true)

        every { preferences.getString(any(), any()) } returns null
        every { context.getSharedPreferences(any(), any()) } returns preferences

        val cdnModule = DownloadCDNModule()
        val baseGson = SerializationModule().baseGson()
        val environmentSetup = EnvironmentSetup(context = context, gson = baseGson)
        val httpClient = HttpModule().defaultHttpClient()
        val cdnClient = cdnModule.cdnHttpClient(httpClient)
        val url = cdnModule.provideDownloadServerUrl(environmentSetup)
        val verificationKeys = VerificationKeys(environmentSetup)
        val gsonFactory = GsonConverterFactory.create()

        val statisticsServer = StatisticsServer(
            api = {
                StatisticsModule().api(
                    client = cdnClient,
                    url = url,
                    gsonConverterFactory = gsonFactory,
                    cache = cache
                )
            },
            cache = cache,
            verificationKeys = verificationKeys
        )

        return runBlocking {
            try {
                val rawData = statisticsServer.getRawStatistics()
                StatisticsParser().parse(rawData)
            } catch (e: Exception) {
                Timber.e(e, "Can't download statistics data. Check your internet connection.")
                null
            }
        }
    }

    val statisticsData: StatisticsData? = loadRealStatisticsData()
}
