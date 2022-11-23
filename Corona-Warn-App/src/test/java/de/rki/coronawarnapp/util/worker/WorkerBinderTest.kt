package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.ListenableWorker
import com.google.gson.Gson
import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.ccl.configuration.update.CclConfigurationUpdater
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTrigger
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultScheduler
import de.rki.coronawarnapp.coronatest.type.pcr.notification.PCRTestResultAvailableNotificationService
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultScheduler
import de.rki.coronawarnapp.covidcertificate.CovidCertificateSettingsDataStore
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateChecker
import de.rki.coronawarnapp.covidcertificate.expiration.DccValidityStateNotification
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.revocation.update.DccRevocationListUpdater
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationCertificateRepository
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationSender
import de.rki.coronawarnapp.deniability.NoiseScheduler
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.familytest.worker.FamilyTestResultRetrievalScheduler
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.notification.GeneralNotifications
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutNotification
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOut
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.storage.retention.TraceLocationDbCleanUpScheduler
import de.rki.coronawarnapp.risk.execution.RiskWorkScheduler
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.di.AppInstallTime
import de.rki.coronawarnapp.util.serialization.BaseGson
import io.github.classgraph.ClassGraph
import io.kotest.matchers.collections.shouldContainAll
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import java.time.Instant
import javax.inject.Provider
import javax.inject.Singleton

class WorkerBinderTest : BaseTest() {

    /**
     * If one of our factories is not part of the factory map provided to **[CWAWorkerFactory]**,
     * then the lookup will fail and an exception thrown.
     * This can't be checked at compile-time and may create subtle errors that will not immediately be caught.
     *
     * This test uses the ClassGraph library to scan our package, find all worker classes,
     * and makes sure that they are all bound into our factory map.
     * Creating a new factory that is not registered or removing one from **[WorkerBinder]**
     * will cause this test to fail.
     */
    @Test
    fun `all worker factory are bound into the factory map`() {
        val component = DaggerWorkerTestComponent.factory().create()
        val factories = component.factories

        Timber.v("We know %d worker factories.", factories.size)
        factories.keys.forEach {
            Timber.v("Registered: ${it.name}")
        }
        require(component.factories.isNotEmpty())

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val ourWorkerClasses = scanResult
            .getSubclasses("androidx.work.ListenableWorker")
            .filterNot { it.name.startsWith("androidx.work") }

        Timber.v("Our project contains %d worker classes.", ourWorkerClasses.size)
        ourWorkerClasses.forEach { Timber.v("Existing: ${it.name}") }

        val boundFactories = factories.keys.map { it.name }
        val existingFactories = ourWorkerClasses.map { it.name }
        boundFactories shouldContainAll existingFactories
    }
}

@Singleton
@Component(modules = [WorkerBinder::class, MockProvider::class])
interface WorkerTestComponent {

    val factories:
        @JvmSuppressWildcards Map<Class<out ListenableWorker>, Provider<InjectedWorkerFactory<out ListenableWorker>>>

    @Component.Factory
    interface Factory {
        fun create(): WorkerTestComponent
    }
}

@Module
class MockProvider {
    // For BackgroundNoiseOneTimeWorker
    @Provides
    fun playbook(): Playbook = mockk()

    // For DeadmanNotificationScheduler
    @Provides
    fun sender(): DeadmanNotificationSender = mockk()

    // For DeadmanNotificationPeriodicWorker
    @Provides
    fun scheduler(): DeadmanNotificationScheduler = mockk()

    // For Analytics periodic worker
    @Provides
    fun dataDonationAnalyticsScheduler(): DataDonationAnalyticsScheduler = mockk()

    // For Analytics one time worker
    @Provides
    fun analytics(): Analytics = mockk()

    // For TraceLocation clean up periodic worker
    @Provides
    fun traceLocationCleanUpScheduler(): TraceLocationDbCleanUpScheduler = mockk()

    // For TraceLocation clean up periodic worker
    @Provides
    fun traceLocationRepository(): TraceLocationRepository = mockk()

    @Provides
    fun taskController(): TaskController = mockk()

    // For ExposureStateUpdateWorker
    @Provides
    fun enfClient(): ENFClient = mockk()

    @Provides
    fun exposureSummaryRepository(): RiskLevelStorage = mockk()

    @Provides
    fun testResultAvailableNotification(): PCRTestResultAvailableNotificationService = mockk()

    @Provides
    fun notificationHelper(): GeneralNotifications = mockk()

    @Provides
    @AppContext
    fun context(): Context = mockk()

    @Provides
    @AppInstallTime
    fun installTime(@AppContext context: Context): Instant = Instant.EPOCH

    @Provides
    @BaseGson
    fun baseGson(): Gson = mockk()

    @Provides
    fun autoCheckOut(): AutoCheckOut = mockk()

    @Provides
    fun checkOutNotification(): CheckOutNotification = mockk()

    @Provides
    fun riskWorkScheduler(): RiskWorkScheduler = mockk()

    @Provides
    fun submissionRepository(): SubmissionRepository = mockk()

    @Provides
    fun coronaTestRepository(): CoronaTestRepository = mockk()

    @Provides
    fun noiseScheduler(): NoiseScheduler = mockk()

    @Provides
    fun pcrTestResultScheduler(): PCRResultScheduler = mockk()

    @Provides
    fun ratResultScheduler(): RAResultScheduler = mockk()

    @Provides
    fun vaccinationRepository(): VaccinationCertificateRepository = mockk()

    @Provides
    fun testCertificateRepository(): TestCertificateRepository = mockk()

    @Provides
    fun dccStateChecker(): DccStateChecker = mockk()

    @Provides
    fun dscCheckNotification(): DccValidityStateNotification = mockk()

    @Provides
    fun recoveryCertificateRepository(): RecoveryCertificateRepository = mockk()

    @Provides
    fun cclConfigurationUpdater(): CclConfigurationUpdater = mockk()

    @Provides
    fun dccWalletInfoUpdateTrigger(): DccWalletInfoUpdateTrigger = mockk()

    @Provides
    fun familyTestScheduler(): FamilyTestResultRetrievalScheduler = mockk()

    @Provides
    fun familyTestRepository(): FamilyTestRepository = mockk()

    @Provides
    fun revocationUpdater(): DccRevocationListUpdater = mockk()

    @CovidCertificateSettingsDataStore
    @Provides
    fun provideCovidCertificateSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)
}
