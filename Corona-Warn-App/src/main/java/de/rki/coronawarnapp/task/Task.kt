package de.rki.coronawarnapp.task

import de.rki.coronawarnapp.util.ui.LazyString
import kotlinx.coroutines.flow.Flow

interface Task<P : Task.Progress> {

    val progress: Flow<P>

    suspend fun run()

    suspend fun cancel()

    interface Progress {
        val primaryMessage: LazyString
    }
}
