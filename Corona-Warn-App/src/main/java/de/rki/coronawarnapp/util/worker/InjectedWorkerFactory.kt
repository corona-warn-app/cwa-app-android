package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

interface InjectedWorkerFactory<T : ListenableWorker> {
    fun create(context: Context, workerParams: WorkerParameters): T
}
