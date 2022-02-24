package de.rki.coronawarnapp.dccreissuance.core.server

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.environment.dccreissuance.DccReissuanceServerURL
import de.rki.coronawarnapp.http.HttpClientDefault
import de.rki.coronawarnapp.http.HttpErrorParser
import de.rki.coronawarnapp.tag
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

@Module
object DccReissuanceServerModule {

    @Provides
    fun provideApi(
        @DccReissuanceServerURL url: String,
        @HttpClientDefault defaultClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): DccReissuanceApi {
        val client = defaultClient.newBuilder()
            .apply {
                // Remove http error parser for custom error handling
                interceptors()
                    .removeAll { it is HttpErrorParser }
                    .also { Timber.tag(TAG).d("Removed %s? %b", tag<HttpErrorParser>(), it) }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(DccReissuanceApi::class.java)
    }

    private val TAG = tag<DccReissuanceServerModule>()
}
