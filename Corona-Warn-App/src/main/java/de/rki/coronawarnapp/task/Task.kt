package de.rki.coronawarnapp.task

import androidx.annotation.Keep
import de.rki.coronawarnapp.util.ui.LazyString
import kotlinx.coroutines.flow.Flow

@Keep
interface Task<
    out ProgressType : Task.Progress,
    out ResultType : Task.Result
    > {

    val progress: Flow<ProgressType>

    suspend fun run(arguments: Arguments): ResultType

    suspend fun cancel()

    interface Progress {
        val primaryMessage: LazyString
    }

    interface Arguments

    interface Result
}
