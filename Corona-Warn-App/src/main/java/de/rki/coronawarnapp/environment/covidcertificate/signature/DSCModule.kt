package de.rki.coronawarnapp.environment.covidcertificate.signature

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.environment.BaseEnvironmentModule
import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.http.HttpClientDefault
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
class DSCModule : BaseEnvironmentModule() {

    @Reusable
    @Provides
    fun getCertificatePinner() = CertificatePinner.Builder()
        .add(
            "de.dscg.ubirch.com",
            "sha256/n8x/JZc4jr86840pnUFowKmymbaOsFjnCujtYQCw8Jc="
        )
        .add(
            "de.test.dscg.ubirch.com",
            "sha256/fkLr78P+zskUzlDRGDdIrjBl04h/AP7/VVHn/6OFFao= "
        )
        .build()

    @Reusable
    @DSCHttpClient
    @Provides
    fun dccHttpClient(
        @HttpClientDefault defaultHttpClient: OkHttpClient,
        certificatePinner: CertificatePinner
    ): OkHttpClient =
        defaultHttpClient.newBuilder()
            .certificatePinner(certificatePinner)
            .build()

    @Singleton
    @DSCServerUrl
    @Provides
    fun dccServerUrl(environment: EnvironmentSetup): String {
        val url = environment.dscServerUrl
        return requireValidUrl(url)
    }
}
