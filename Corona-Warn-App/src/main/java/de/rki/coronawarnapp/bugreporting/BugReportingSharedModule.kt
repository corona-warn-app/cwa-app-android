package de.rki.coronawarnapp.bugreporting

import dagger.Binds
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
import de.rki.coronawarnapp.bugreporting.censors.family.FamilyTestCensor
import de.rki.coronawarnapp.bugreporting.censors.presencetracing.CheckInsCensor
import de.rki.coronawarnapp.bugreporting.censors.presencetracing.TraceLocationCensor
import de.rki.coronawarnapp.bugreporting.censors.profile.ProfileCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.CoronaTestCertificateCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.OtpCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.PcrQrCodeCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.PcrTeleTanCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.RACoronaTestCensor
import de.rki.coronawarnapp.bugreporting.censors.submission.RapidQrCodeCensor
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebugLoggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage.UploadHistoryStorageModule
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
@Module(
    includes = [
        BugReportingSharedModule.BindsModule::class,
        UploadHistoryStorageModule::class
    ]
)
object BugReportingSharedModule {

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

    @Module
    internal interface BindsModule {

        @Binds
        @IntoSet
        fun registrationTokenCensor(censor: CoronaTestCensor): BugCensor

        @Binds
        @IntoSet
        fun testCertificateCensor(censor: CoronaTestCertificateCensor): BugCensor

        @Binds
        @IntoSet
        fun pcrQrCodeCensor(censor: PcrQrCodeCensor): BugCensor

        @Binds
        @IntoSet
        fun pcrTeleTanCensor(censor: PcrTeleTanCensor): BugCensor

        @Binds
        @IntoSet
        fun rapidQrCodeCensor(censor: RapidQrCodeCensor): BugCensor

        @Binds
        @IntoSet
        fun raCoronaTestCensor(censor: RACoronaTestCensor): BugCensor

        @Binds
        @IntoSet
        fun diaryPersonCensor(censor: DiaryPersonCensor): BugCensor

        @Binds
        @IntoSet
        fun diaryEncounterCensor(censor: DiaryEncounterCensor): BugCensor

        @Binds
        @IntoSet
        fun diaryLocationCensor(censor: DiaryLocationCensor): BugCensor

        @Binds
        @IntoSet
        fun diaryVisitCensor(censor: DiaryVisitCensor): BugCensor

        @Binds
        @IntoSet
        fun checkInsCensor(censor: CheckInsCensor): BugCensor

        @Binds
        @IntoSet
        fun traceLocationsCensor(censor: TraceLocationCensor): BugCensor

        @Binds
        @IntoSet
        fun profileCensor(censor: ProfileCensor): BugCensor

        @Binds
        @IntoSet
        fun certificateQrCodeCensor(censor: DccQrCodeCensor): BugCensor

        @Binds
        @IntoSet
        fun organizerRegistrationTokenCensor(censor: OrganizerRegistrationTokenCensor): BugCensor

        @Binds
        @IntoSet
        fun cwaUserCensor(censor: CwaUserCensor): BugCensor

        @Binds
        @IntoSet
        fun ticketingJwtCensor(censor: DccTicketingJwtCensor): BugCensor

        @Binds
        @IntoSet
        fun familyTestCensor(censor: FamilyTestCensor): BugCensor

        @Binds
        @IntoSet
        fun otpCensor(censor: OtpCensor): BugCensor
    }
}
