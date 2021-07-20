package de.rki.coronawarnapp.environment.covidcertificate.signature

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
class DSCModule : BaseEnvironmentModule() {

    @Reusable
    @DSCHttpClient
    @Provides
    fun dccHttpClient(@HttpClientDefault defaultHttpClient: OkHttpClient): OkHttpClient =
        defaultHttpClient.newBuilder().build()

    @Singleton
    @DSCServerUrl
    @Provides
    fun dccServerUrl(environment: EnvironmentSetup): String {
        val url = environment.dscServerUrl
        return requireValidUrl(url)
    }
}
