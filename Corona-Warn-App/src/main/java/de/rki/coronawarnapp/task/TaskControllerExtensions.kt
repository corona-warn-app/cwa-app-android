package de.rki.coronawarnapp.task

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

suspend fun TaskController.submitBlocking(ourRequest: TaskRequest) =
    tasks.flatMapMerge { it.asFlow() }.map { it.taskState }.onStart {
        submit(ourRequest)
        Timber.v("submitBlocking(request=%s) waiting for result...", ourRequest)
    }.first {
        it.request.id == ourRequest.id && it.isFinished
    }.also {
        Timber.v("submitBlocking(request=%s) continuing with result %s", ourRequest, it)
    }
