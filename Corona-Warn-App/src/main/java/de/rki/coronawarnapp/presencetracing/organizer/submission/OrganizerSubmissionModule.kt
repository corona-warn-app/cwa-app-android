package de.rki.coronawarnapp.presencetracing.organizer.submission

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.environment.submission.SubmissionCDNServerUrl
import de.rki.coronawarnapp.presencetracing.organizer.submission.server.OrganizerSubmissionApiV1
import de.rki.coronawarnapp.submission.server.SubmissionHttpClient
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory

@InstallIn(SingletonComponent::class)
@Module
class OrganizerSubmissionModule {

    @Reusable
    @Provides
    fun provideOrganizerSubmissionApiV1(
        @SubmissionHttpClient client: OkHttpClient,
        @SubmissionCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory
    ): OrganizerSubmissionApiV1 = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(protoConverterFactory)
        .build()
        .create(OrganizerSubmissionApiV1::class.java)
}
