package de.rki.coronawarnapp.util.worker

import androidx.work.ListenableWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.deadman.DeadmanNotificationOneTimeWorker
import de.rki.coronawarnapp.deadman.DeadmanNotificationPeriodicWorker
import de.rki.coronawarnapp.nearby.ExposureStateUpdateWorker
import de.rki.coronawarnapp.worker.BackgroundNoiseOneTimeWorker
import de.rki.coronawarnapp.worker.BackgroundNoisePeriodicWorker
import de.rki.coronawarnapp.worker.DiagnosisKeyRetrievalOneTimeWorker
import de.rki.coronawarnapp.worker.DiagnosisKeyRetrievalPeriodicWorker
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
    @WorkerKey(DiagnosisKeyRetrievalOneTimeWorker::class)
    abstract fun diagnosisKeyRetrievalOneTime(
        factory: DiagnosisKeyRetrievalOneTimeWorker.Factory
    ): InjectedWorkerFactory<out ListenableWorker>

    @Binds
    @IntoMap
    @WorkerKey(DiagnosisKeyRetrievalPeriodicWorker::class)
    abstract fun diagnosisKeyRetrievalPeriodic(
        factory: DiagnosisKeyRetrievalPeriodicWorker.Factory
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
}
