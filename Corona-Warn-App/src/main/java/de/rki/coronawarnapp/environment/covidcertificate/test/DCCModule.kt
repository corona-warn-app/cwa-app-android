package de.rki.coronawarnapp.environment.covidcertificate.test

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.OkHttpClient
import javax.inject.Singleton

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
