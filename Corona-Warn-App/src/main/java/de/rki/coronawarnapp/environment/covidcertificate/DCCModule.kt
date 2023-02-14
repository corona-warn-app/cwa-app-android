package de.rki.coronawarnapp.environment.covidcertificate

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.OkHttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DCCModule : BaseEnvironmentModule() {

    @Reusable
    @DCCHttpClient
    @Provides
    fun dccHttpClient(@HttpClientDefault defaultHttpClient: OkHttpClient): OkHttpClient =
        defaultHttpClient.newBuilder().build()

    @Singleton
    @DCCServerUrl
    @Provides
    fun dccServerUrl(environment: EnvironmentSetup): String {
        val url = environment.dccServerUrl
        return requireValidUrl(url)
    }
}
