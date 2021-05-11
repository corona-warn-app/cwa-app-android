package de.rki.coronawarnapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkManager
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.appconfig.ConfigChangeDetector
import de.rki.coronawarnapp.appconfig.devicetime.DeviceTimeHandler
import de.rki.coronawarnapp.bugreporting.loghistory.LogHistoryTree
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryWorkScheduler
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.notification.ShareTestResultNotificationService
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultScheduler
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler
import de.rki.coronawarnapp.coronatest.type.rapidantigen.notification.RATTestResultAvailableNotificationService
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.exception.reporting.ErrorReportReceiver
import de.rki.coronawarnapp.exception.reporting.ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOut
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingRiskWorkScheduler
import de.rki.coronawarnapp.presencetracing.storage.retention.TraceLocationDbCleanUpScheduler
import de.rki.coronawarnapp.risk.RiskLevelChangeDetector
import de.rki.coronawarnapp.risk.execution.ExposureWindowRiskWorkScheduler
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.WatchdogService
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.util.hasAPILevel
import de.rki.coronawarnapp.vaccination.core.execution.VaccinationUpdateScheduler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.conscrypt.Conscrypt
import timber.log.Timber
import java.security.Security
import javax.inject.Inject

class CoronaWarnApplication : Application(), HasAndroidInjector {

    @Inject lateinit var component: ApplicationComponent

    @Inject lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    @Inject lateinit var watchdogService: WatchdogService
    @Inject lateinit var taskController: TaskController
    @Inject lateinit var foregroundState: ForegroundState
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var configChangeDetector: ConfigChangeDetector
    @Inject lateinit var riskLevelChangeDetector: RiskLevelChangeDetector
    @Inject lateinit var deadmanNotificationScheduler: DeadmanNotificationScheduler
    @Inject lateinit var contactDiaryWorkScheduler: ContactDiaryWorkScheduler
    @Inject lateinit var dataDonationAnalyticsScheduler: DataDonationAnalyticsScheduler
    @Inject lateinit var notificationHelper: GeneralNotifications
    @Inject lateinit var deviceTimeHandler: DeviceTimeHandler
    @Inject lateinit var autoSubmission: AutoSubmission
    @Inject lateinit var coronaTestRepository: CoronaTestRepository
    @Inject lateinit var autoCheckOut: AutoCheckOut
    @Inject lateinit var traceLocationDbCleanupScheduler: TraceLocationDbCleanUpScheduler
    @Inject lateinit var shareTestResultNotificationService: ShareTestResultNotificationService
    @Inject lateinit var exposureWindowRiskWorkScheduler: ExposureWindowRiskWorkScheduler
    @Inject lateinit var presenceTracingRiskWorkScheduler: PresenceTracingRiskWorkScheduler
    @Inject lateinit var pcrTestResultScheduler: PCRResultScheduler
    @Inject lateinit var raTestResultScheduler: RAResultScheduler
    @Inject lateinit var pcrTestResultAvailableNotificationService: PCRTestResultAvailableNotificationService
    @Inject lateinit var raTestResultAvailableNotificationService: RATTestResultAvailableNotificationService
    @Inject lateinit var vaccinationUpdateScheduler: VaccinationUpdateScheduler

    @LogHistoryTree @Inject lateinit var rollingLogHistory: Timber.Tree

    @Inject lateinit var vaccinationProofServer: VaccinationProofServer

    override fun onCreate() {
        instance = this
        super.onCreate()
        CWADebug.init(this)

        AppInjector.init(this).let { compPreview ->
            if (BuildVersionWrap.hasAPILevel(23)) {
                Timber.v("Calling EncryptedPreferencesMigration.doMigration()")
                compPreview.encryptedMigration.get().doMigration()
            }

            CWADebug.initAfterInjection(compPreview)

            Timber.v("Completing application injection")
            compPreview.inject(this)
        }

        Timber.plant(rollingLogHistory)

        Timber.v("onCreate(): WorkManager setup done: $workManager")

        // Enable Conscrypt for TLS1.3 Support below API Level 29
        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        registerActivityLifecycleCallbacks(activityLifecycleCallback)

        watchdogService.launch()

        foregroundState.isInForeground
            .onEach { isAppInForeground = it }
            .launchIn(GlobalScope)

        Timber.v("Setting up contact diary work scheduler")
        contactDiaryWorkScheduler.setup()

        Timber.v("Setting up deadman notification scheduler")
        deadmanNotificationScheduler.setup()

        Timber.v("Setting up risk work schedulers.")
        exposureWindowRiskWorkScheduler.setup()
        presenceTracingRiskWorkScheduler.setup()

        Timber.v("Setting up test result work schedulers.")
        pcrTestResultScheduler.setup()
        raTestResultScheduler.setup()

        Timber.v("Setting up test result available notification services.")
        pcrTestResultAvailableNotificationService.setup()
        raTestResultAvailableNotificationService.setup()

        Timber.v("Setting up vaccination data update scheduler.")
        vaccinationUpdateScheduler.setup()

        deviceTimeHandler.launch()
        configChangeDetector.launch()
        riskLevelChangeDetector.launch()
        autoSubmission.setup()
        autoCheckOut.setupMonitor()
        traceLocationDbCleanupScheduler.scheduleDaily()
        shareTestResultNotificationService.setup()
//
//        val testData =
//            "6BFOXN*TS0BI\$ZD4N9:9S6RCVN5+O30K3/XIV0W23NTDEXWK G2EP4J0BGJLFX3R3VHXK.PJ:2DPF6R:5SVBHABVCNN95SWMPHQUHQN%A0SOE+QQAB-HQ/HQ7IR.SQEEOK9SAI4- 7Y15KBPD34 QWSP0WRGTQFNPLIR.KQNA7N95U/3FJCTG90OARH9P1J4HGZJKBEG%123ZC\$0BCI757TLXKIBTV5TN%2LXK-\$CH4TSXKZ4S/\$K%0KPQ1HEP9.PZE9Q\$95:UENEUW6646936HRTO\$9KZ56DE/.QC\$Q3J62:6LZ6O59++9-G9+E93ZM\$96TV6NRN3T59YLQM1VRMP\$I/XK\$M8PK66YBTJ1ZO8B-S-*O5W41FD\$ 81JP%KNEV45G1H*KESHMN2/TU3UQQKE*QHXSMNV25\$1PK50C9B/9OK5NE1 9V2:U6A1ELUCT16DEETUM/UIN9P8Q:KPFY1W+UN MUNU8T1PEEG%5TW5A 6YO67N6BBEWED/3LS3N6YU.:KJWKPZ9+CQP2IOMH.PR97QC:ACZAH.SYEDK3EL-FIK9J8JRBC7ADHWQYSK48UNZGG NAVEHWEOSUI2L.9OR8FHB0T5HM7I"
//        val base45Decoder = Base45Decoder()
//        val zLIBDecompressor = ZLIBDecompressor()
//
//        val gabrielleHex =
//            "d2844da20448d919375fc1e7b6b20126a0590124a4041a609cf467061a609a516701624154390103a101a4617681aa62646e01626d616d4f52472d3130303033303231356276706a313131393330353030356264746a323032312d30322d313862636f624154626369783075726e3a757663693a30313a41543a313038303738343346393441454530454535303933464243323534424438313350626d706c45552f312f32302f313532386269736e424d5347504b20417573747269616273640262746769383430353339303036636e616da463666e74754d5553544552465241553c474f455353494e47455262666e754d7573746572667261752d47c3b6c39f696e67657263676e74684741425249454c4562676e684761627269656c656376657265312e302e3063646f626a313939382d30322d32365840273583393b862f0fa3e09805e7124c5a32cba81f7e0bb7fff99a14b8cf1e103faeab326703b011966dc5fe29ccaec55d4a689b9981967c88d312bbb441fd9ffd"
//                .decodeHex()
//        val techSpecHex =
//            "d28443a10126a104508ede3316d4da418181f0753affc6a3a359013ba401624445041a60b8d32f061a6082879b390103a101a4617681aa6263697831303144452f38343530332f313131393334393030372f44585347574c574c34305355385a464b495949424b33394133235362636f62444562646e026264746a323032312d30322d3032626973782142756e6465736d696e697374657269756d2066c3bc7220476573756e6468656974626d616d4f52472d313030303330323135626d706c45552f312f32302f3135323862736402627467693834303533393030366276706a3131313933343930303763646f626a313936342d30382d3132636e616da462666e725363686d697474204d75737465726d616e6e62676e6c4572696b612044c3b672746563666e74725343484d4954543c4d55535445524d414e4e63676e746c4552494b413c444f455254456376657265312e302e305840d93d362f06a909dc69260149d917498b88ab21ec8658cdbb220d25610f8c858784abd1d488f48153075b0ccbb55a54dd445599dc72db55bd63a50291fba44921"
//                .decodeHex()
////        val forTheServer = testData
////            .let { zLIBDecompressor.decode(base45Decoder.decode(it)) }
//
//        GlobalScope.launch {
//            vaccinationProofServer.getProofCertificate(
//                techSpecHex.toByteArray()
//            )
//        }
    }

    private val activityLifecycleCallback = object : ActivityLifecycleCallbacks {
        private val localBM by lazy {
            LocalBroadcastManager.getInstance(this@CoronaWarnApplication)
        }
        private var errorReceiver: ErrorReportReceiver? = null

        override fun onActivityPaused(activity: Activity) {
            errorReceiver?.let {
                localBM.unregisterReceiver(it)
                errorReceiver = null
            }
            disableAppLauncherPreviewAndScreenshots(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            enableAppLauncherPreviewAndScreenshots(activity)
        }

        override fun onActivityDestroyed(activity: Activity) {
            // NOOP
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // NOOP
        }

        override fun onActivityStopped(activity: Activity) {
            disableAppLauncherPreviewAndScreenshots(activity)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // NOOP
        }

        override fun onActivityResumed(activity: Activity) {
            errorReceiver?.let {
                localBM.unregisterReceiver(it)
                errorReceiver = null
            }

            errorReceiver = ErrorReportReceiver(activity).also {
                localBM.registerReceiver(it, IntentFilter(ERROR_REPORT_LOCAL_BROADCAST_CHANNEL))
            }
            enableAppLauncherPreviewAndScreenshots(activity)
        }
    }

    private fun enableAppLauncherPreviewAndScreenshots(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun disableAppLauncherPreviewAndScreenshots(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    companion object {
        private lateinit var instance: CoronaWarnApplication

        /* describes if the app is in foreground
         * Initialized to false, because app could also be started by a background job.
         * For the cases where the app is started via the launcher icon, the onAppForegrounded
         * event will be called, setting it to true
         */
        var isAppInForeground = false

        fun getAppContext(): Context = instance.applicationContext
    }
}
