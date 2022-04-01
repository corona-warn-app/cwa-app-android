package de.rki.coronawarnapp.util.worker

import androidx.work.ListenableWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.ccl.configuration.update.CclConfigurationUpdateWorker
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryRetentionWorker
import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultRetrievalWorker
import de.rki.coronawarnapp.coronatest.type.rapidantigen.execution.RAResultRetrievalWorker
import de.rki.coronawarnapp.covidcertificate.common.statecheck.DccStateCheckWorker
import de.rki.coronawarnapp.covidcertificate.revocation.update.RevocationUpdateWorker
import de.rki.coronawarnapp.covidcertificate.test.core.execution.TestCertificateRetrievalWorker
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsPeriodicWorker
import de.rki.coronawarnapp.deadman.DeadmanNotificationOneTimeWorker
import de.rki.coronawarnapp.deadman.DeadmanNotificationPeriodicWorker
import de.rki.coronawarnapp.deniability.BackgroundNoiseOneTimeWorker
import de.rki.coronawarnapp.deniability.BackgroundNoisePeriodicWorker
import de.rki.coronawarnapp.diagnosiskeys.execution.DiagnosisKeyRetrievalWorker
import de.rki.coronawarnapp.nearby.ExposureStateUpdateWorker
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOutWorker
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningWorker
import de.rki.coronawarnapp.presencetracing.storage.retention.TraceLocationDbCleanUpPeriodicWorker
import de.rki.coronawarnapp.submission.auto.SubmissionWorker

@Module
abstract class WorkerBinder {

    @Binds
    @IntoMap
    @WorkerKey(ExposureStateUpdateWorker::class)
    abstract fun bindExposureStateUpdate(
        factory: ExposureStateUpdateWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(BackgroundNoiseOneTimeWorker::class)
    abstract fun backgroundNoiseOneTime(
        factory: BackgroundNoiseOneTimeWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(BackgroundNoisePeriodicWorker::class)
    abstract fun backgroundNoisePeriodic(
        factory: BackgroundNoisePeriodicWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(DiagnosisKeyRetrievalWorker::class)
    abstract fun diagnosisKeyRetrievalOneTime(
        factory: DiagnosisKeyRetrievalWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(PCRResultRetrievalWorker::class)
    abstract fun pcrTestResultRetrievalPeriodic(
        factory: PCRResultRetrievalWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(RAResultRetrievalWorker::class)
    abstract fun ratResultRetrievalPeriodic(
        factory: RAResultRetrievalWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(DeadmanNotificationOneTimeWorker::class)
    abstract fun deadmanNotificationOneTime(
        factory: DeadmanNotificationOneTimeWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(DeadmanNotificationPeriodicWorker::class)
    abstract fun deadmanNotificationPeriodic(
        factory: DeadmanNotificationPeriodicWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(SubmissionWorker::class)
    abstract fun submissionBackgroundWorker(
        factory: SubmissionWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(ContactDiaryRetentionWorker::class)
    abstract fun contactDiaryCleanWorker(
        factory: ContactDiaryRetentionWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(DataDonationAnalyticsPeriodicWorker::class)
    abstract fun dataDonationAnalyticsPeriodicWorker(
        factory: DataDonationAnalyticsPeriodicWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(AutoCheckOutWorker::class)
    abstract fun autoCheckOutWorker(
        factory: AutoCheckOutWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(TraceLocationDbCleanUpPeriodicWorker::class)
    abstract fun traceLocationCleanUpWorker(
        factory: TraceLocationDbCleanUpPeriodicWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(PresenceTracingWarningWorker::class)
    abstract fun traceWarningWorker(
        factory: PresenceTracingWarningWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(TestCertificateRetrievalWorker::class)
    abstract fun testCertificateRetrievalWorker(
        factory: TestCertificateRetrievalWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(DccStateCheckWorker::class)
    abstract fun dccStateCheckWorker(
        factory: DccStateCheckWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(CclConfigurationUpdateWorker::class)
    abstract fun cclConfigurationUpdateWorker(
        factory: CclConfigurationUpdateWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(RevocationUpdateWorker::class)
    abstract fun revocationUpdateWorker(
        factory: RevocationUpdateWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>
}
