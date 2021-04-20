package de.rki.coronawarnapp.util.worker

import androidx.work.ListenableWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryRetentionWorker
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsPeriodicWorker
import de.rki.coronawarnapp.deadman.DeadmanNotificationOneTimeWorker
import de.rki.coronawarnapp.deadman.DeadmanNotificationPeriodicWorker
import de.rki.coronawarnapp.deniability.BackgroundNoiseOneTimeWorker
import de.rki.coronawarnapp.deniability.BackgroundNoisePeriodicWorker
import de.rki.coronawarnapp.diagnosiskeys.execution.DiagnosisKeyRetrievalWorker
import de.rki.coronawarnapp.presencetracing.storage.retention.TraceLocationDbCleanUpPeriodicWorker
import de.rki.coronawarnapp.nearby.ExposureStateUpdateWorker
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOutWorker
import de.rki.coronawarnapp.presencetracing.risk.execution.PresenceTracingWarningWorker
import de.rki.coronawarnapp.submission.auto.SubmissionWorker
import de.rki.coronawarnapp.worker.DiagnosisTestResultRetrievalPeriodicWorker

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
    @WorkerKey(DiagnosisTestResultRetrievalPeriodicWorker::class)
    abstract fun testResultRetrievalPeriodic(
        factory: DiagnosisTestResultRetrievalPeriodicWorker.Factory
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
}
