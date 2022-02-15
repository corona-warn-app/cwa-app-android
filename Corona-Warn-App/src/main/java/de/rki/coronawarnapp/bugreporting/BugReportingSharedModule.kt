package de.rki.coronawarnapp.bugreporting

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.contactdiary.DiaryEncounterCensor
import de.rki.coronawarnapp.bugreporting.censors.contactdiary.DiaryLocationCensor
import de.rki.coronawarnapp.bugreporting.censors.contactdiary.DiaryPersonCensor
import de.rki.coronawarnapp.bugreporting.censors.contactdiary.DiaryVisitCensor
import de.rki.coronawarnapp.bugreporting.censors.contactdiary.OrganizerRegistrationTokenCensor
import de.rki.coronawarnapp.bugreporting.censors.dcc.CwaUserCensor
import de.rki.coronawarnapp.bugreporting.censors.dcc.DccQrCodeCensor
import de.rki.coronawarnapp.bugreporting.censors.dccticketing.DccTicketingJwtCensor
import de.rki.coronawarnapp.bugreporting.censors.presencetracing.CheckInsCensor
import de.rki.coronawarnapp.bugreporting.censors.presencetracing.TraceLocationCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCertificateCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.PcrQrCodeCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.PcrTeleTanCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.RACoronaTestCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.RatProfileCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.RapidQrCodeCensor
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLoggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.LogUploadApiV1
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadAuthApiV1
import de.rki.coronawarnapp.environment.bugreporting.LogUploadHttpClient
import de.rki.coronawarnapp.environment.bugreporting.LogUploadServerUrl
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNHttpClient
import de.rki.coronawarnapp.environment.datadonation.DataDonationCDNServerUrl
import de.rki.coronawarnapp.util.CWADebug
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Module
class BugReportingSharedModule {

    @Reusable
    @Provides
    fun logUploadApi(
        @LogUploadHttpClient client: OkHttpClient,
        @LogUploadServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): LogUploadApiV1 = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(protoConverterFactory)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(LogUploadApiV1::class.java)

    @Reusable
    @Provides
    fun logUploadAuthApi(
        @DataDonationCDNHttpClient client: OkHttpClient,
        @DataDonationCDNServerUrl url: String,
        protoConverterFactory: ProtoConverterFactory,
        gsonConverterFactory: GsonConverterFactory
    ): LogUploadAuthApiV1 = Retrofit.Builder()
        .client(client)
        .baseUrl(url)
        .addConverterFactory(protoConverterFactory)
        .addConverterFactory(gsonConverterFactory)
        .build()
        .create(LogUploadAuthApiV1::class.java)

    @Singleton
    @Provides
    fun debugLogger() = CWADebug.debugLogger

    @Singleton
    @DebuggerScope
    @Provides
    fun scope(): CoroutineScope = DebugLoggerScope

    @Provides
    @IntoSet
    fun registrationTokenCensor(censor: CoronaTestCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun testCertificateCensor(censor: CoronaTestCertificateCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun pcrQrCodeCensor(censor: PcrQrCodeCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun pcrTeleTanCensor(censor: PcrTeleTanCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun ratQrCodeCensor(censor: RapidQrCodeCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun raCoronaTestCensor(censor: RACoronaTestCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun diaryPersonCensor(censor: DiaryPersonCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun diaryEncounterCensor(censor: DiaryEncounterCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun diaryLocationCensor(censor: DiaryLocationCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun diaryVisitCensor(censor: DiaryVisitCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun checkInsCensor(censor: CheckInsCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun traceLocationsCensor(censor: TraceLocationCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun ratProfileCensor(censor: RatProfileCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun certificateQrCodeCensor(censor: DccQrCodeCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun organizerRegistrationTokenCensor(censor: OrganizerRegistrationTokenCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun cwaUserCensor(censor: CwaUserCensor): BugCensor = censor

    @Provides
    @IntoSet
    fun ticketingJwtCensor(censor: DccTicketingJwtCensor): BugCensor = censor
}
