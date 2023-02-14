package de.rki.coronawarnapp.environment.datadonation

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
class DataDonationCDNModule : BaseEnvironmentModule() {

    @Reusable
    @DataDonationCDNHttpClient
    @Provides
    fun cdnHttpClient(@HttpClientDefault defaultHttpClient: OkHttpClient): OkHttpClient =
        defaultHttpClient.newBuilder().build()

    @Singleton
    @DataDonationCDNServerUrl
    @Provides
    fun provideDonationServerUrl(environment: EnvironmentSetup): String {
        val url = environment.dataDonationCdnUrl
        return requireValidUrl(url)
    }
}
