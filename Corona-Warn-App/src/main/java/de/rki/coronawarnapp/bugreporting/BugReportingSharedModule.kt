package de.rki.coronawarnapp.bugreporting

import dagger.Module
import dagger.Provides
import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.DiaryLocationCensor
import de.rki.coronawarnapp.bugreporting.censors.DiaryPersonCensor
import de.rki.coronawarnapp.bugreporting.censors.QRCodeCensor
import de.rki.coronawarnapp.bugreporting.censors.RegistrationTokenCensor
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLoggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.bugreporting.logupload.auth.LogUploadAuthApiV1
import de.rki.coronawarnapp.bugreporting.logupload.server.LogUploadApi
import de.rki.coronawarnapp.environment.bugreporting.LogUploadHttpClient
import de.rki.coronawarnapp.environment.bugreporting.LogUploadServerUrl
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import de.rki.coronawarnapp.util.CWADebug
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.protobuf.ProtoConverterFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
class BugReportingSharedModule {

    @Reusable
    @Provides
    fun logUploadApi(
        @LogUploadHttpClient client: OkHttpClient,
        @LogUploadServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory
    ): LogUploadApi = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(protoConverterFactory)
        .build()
        .create(LogUploadApi::class.java)

    @Reusable
    @Provides
    fun logUploadAuthApi(
        @DataDonationCDNHttpClient client: OkHttpClient,
        @DataDonationCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory
    ): LogUploadAuthApiV1 = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(protoConverterFactory)
        .build()
        .create(LogUploadAuthApiV1::class.java)

    @Singleton
    @Provides
    fun debugLogger() = CWADebug.debugLogger

    @Singleton
    @DebuggerScope
    @Provides
    fun scope(): CoroutineScope = DebugLoggerScope

    @Singleton
    @Provides
    fun censors(
        registrationTokenCensor: RegistrationTokenCensor,
        diaryPersonCensor: DiaryPersonCensor,
        diaryLocationCensor: DiaryLocationCensor,
        qrCodeCensor: QRCodeCensor
    ): List<BugCensor> = listOf(
        registrationTokenCensor,
        diaryPersonCensor,
        diaryLocationCensor,
        qrCodeCensor
    ).also {
        Timber.d("Loaded BugCensors: %s", it)
    }
}
