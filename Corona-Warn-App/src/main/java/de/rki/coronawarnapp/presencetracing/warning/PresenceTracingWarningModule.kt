package de.rki.coronawarnapp.presencetracing.warning

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.environment.download.DownloadCDNHttpClient
import de.rki.coronawarnapp.environment.download.DownloadCDNServerUrl
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningTask
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceWarningEncryptedApiV2
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceWarningUnencryptedApiV1
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.task.TaskFactory
import de.rki.coronawarnapp.task.TaskTypeKey
import de.rki.coronawarnapp.util.reset.Resettable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [PresenceTracingWarningModule.BindsModule::class])
object PresenceTracingWarningModule {

    @Singleton
    @Provides
    fun apiV1(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory,
    ): TraceWarningUnencryptedApiV1 {

        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(TraceWarningUnencryptedApiV1::class.java)
    }

    @Singleton
    @Provides
    fun apiV2(
        @DownloadCDNHttpClient client: OkHttpClient,
        @DownloadCDNServerUrl url: String,
        gsonConverterFactory: GsonConverterFactory,
    ): TraceWarningEncryptedApiV2 {

        return Retrofit.Builder()
            .client(client)
            .baseUrl(url)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(TraceWarningEncryptedApiV2::class.java)
    }

    @Module
    internal interface BindsModule {

        @Binds
        @IntoMap
        @TaskTypeKey(PresenceTracingWarningTask::class)
        fun taskFactory(
            factory: PresenceTracingWarningTask.Factory
        ): TaskFactory<out Task.Progress, out Task.Result>

        @Binds
        @IntoSet
        fun bindResettableTraceWarningRepository(resettable: TraceWarningRepository): Resettable
    }
}
