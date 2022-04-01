package de.rki.coronawarnapp.covidcertificate.revocation

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.covidcertificate.revocation.server.RevocationApi
import retrofit2.Retrofit

@Module
object RevocationModule {

    @Provides
    // TODO("Add Client and URL)
    fun provideRevocationApi(): RevocationApi = Retrofit.Builder().build().create(RevocationApi::class.java)
}
