package de.rki.coronawarnapp.util.di

import dagger.BindsInstance
import dagger.Component
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.appconfig.AppConfigModule
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.BugReporter
import de.rki.coronawarnapp.bugreporting.BugReportingModule
import de.rki.coronawarnapp.bugreporting.BugReportingSharedModule
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.ccl.CclModule
import de.rki.coronawarnapp.coronatest.CoronaTestModule
import de.rki.coronawarnapp.coronatest.server.VerificationModule
import de.rki.coronawarnapp.covidcertificate.DigitalCovidCertificateModule
import de.rki.coronawarnapp.datadonation.DataDonationModule
import de.rki.coronawarnapp.dccreissuance.DccReissuanceModule
import de.rki.coronawarnapp.dccticketing.DccTicketingModule
import de.rki.coronawarnapp.diagnosiskeys.DiagnosisKeysModule
import de.rki.coronawarnapp.diagnosiskeys.DownloadDiagnosisKeysTaskModule
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.environment.EnvironmentModule
import de.rki.coronawarnapp.familytest.core.FamilyTestModule
import de.rki.coronawarnapp.gstatus.ui.GStatusModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.initializer.InitializerModule
import de.rki.coronawarnapp.main.MainModule
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.nearby.ENFModule
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.presencetracing.PresenceTracingModule
import de.rki.coronawarnapp.profile.ProfileModule
import de.rki.coronawarnapp.qrcode.QrCodeScannerModule
import de.rki.coronawarnapp.receiver.ReceiverBinder
import de.rki.coronawarnapp.risk.RiskModule
import de.rki.coronawarnapp.rootdetection.RootDetectionModule
import de.rki.coronawarnapp.service.ServiceBinder
import de.rki.coronawarnapp.srs.core.SrsSubmissionModule
import de.rki.coronawarnapp.statistics.StatisticsModule
import de.rki.coronawarnapp.storage.StorageModule
import de.rki.coronawarnapp.submission.SubmissionModule
import de.rki.coronawarnapp.submission.task.SubmissionTaskModule
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.internal.TaskModule
import de.rki.coronawarnapp.test.DeviceForTestersModule
import de.rki.coronawarnapp.ui.ActivityBinder
import de.rki.coronawarnapp.update.InAppUpdateModule
import de.rki.coronawarnapp.util.coil.CoilModule
import de.rki.coronawarnapp.util.coroutine.AppCoroutineScope
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.CoroutineModule
import de.rki.coronawarnapp.util.device.DeviceModule
import de.rki.coronawarnapp.util.encryptionmigration.EncryptedPreferencesFactory
import de.rki.coronawarnapp.util.encryptionmigration.EncryptedPreferencesMigration
import de.rki.coronawarnapp.util.encryptionmigration.EncryptionErrorResetTool
import de.rki.coronawarnapp.util.security.SecurityModule
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.worker.WorkerBinder
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        CoroutineModule::class,
        AndroidModule::class,
        ReceiverBinder::class,
        ServiceBinder::class,
        ActivityBinder::class,
        CoilModule::class,
        RiskModule::class,
        DeviceModule::class,
        ENFModule::class,
        HttpModule::class,
        EnvironmentModule::class,
        DiagnosisKeysModule::class,
        AppConfigModule::class,
        SubmissionModule::class,
        SubmissionTaskModule::class,
        DownloadDiagnosisKeysTaskModule::class,
        VerificationModule::class,
        TaskModule::class,
        DeviceForTestersModule::class,
        BugReportingModule::class,
        BugReportingSharedModule::class,
        SerializationModule::class,
        WorkerBinder::class,
        StatisticsModule::class,
        DataDonationModule::class,
        SecurityModule::class,
        PresenceTracingModule::class,
        CoronaTestModule::class,
        DigitalCovidCertificateModule::class,
        QrCodeScannerModule::class,
        RootDetectionModule::class,
        InAppUpdateModule::class,
        DccTicketingModule::class,
        CclModule::class,
        DccReissuanceModule::class,
        GStatusModule::class,
        FamilyTestModule::class,
        ProfileModule::class,
        MainModule::class,
        StorageModule::class,
        InitializerModule::class,
        SrsSubmissionModule::class,
    ]
)
interface ApplicationComponent : AndroidInjector<CoronaWarnApplication> {

    val keyCacheRepository: KeyCacheRepository

    val appConfigProvider: AppConfigProvider

    val enfClient: ENFClient

    val encryptedPreferencesFactory: EncryptedPreferencesFactory

    val errorResetTool: EncryptionErrorResetTool

    val playbook: Playbook

    val taskController: TaskController

    @AppScope val appScope: AppCoroutineScope

    val bugReporter: BugReporter

    fun inject(logger: DebugLogger)

    val encryptedMigration: Lazy<EncryptedPreferencesMigration>

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: CoronaWarnApplication): ApplicationComponent
    }
}
