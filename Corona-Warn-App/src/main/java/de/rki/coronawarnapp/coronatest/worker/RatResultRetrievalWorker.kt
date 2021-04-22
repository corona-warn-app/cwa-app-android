package de.rki.coronawarnapp.coronatest.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.worker.InjectedWorkerFactory

/**
 * Diagnosis test result retrieval by periodic polling
 */
class RatResultRetrievalWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    val coronaTestRepository: CoronaTestRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork() = Result.success().also {
        coronaTestRepository.refresh(CoronaTest.Type.RAPID_ANTIGEN)
    }

    @AssistedFactory
    interface Factory : InjectedWorkerFactory<RatResultRetrievalWorker>
}
