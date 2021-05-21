package de.rki.coronawarnapp.task.common

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.util.ui.CachedString

sealed class DefaultProgress(progressMessage: String) : Task.Progress {
    override val primaryMessage = CachedString { progressMessage }
}

object Started : DefaultProgress("Started")
object Finished : DefaultProgress("Finished")

data class ProgressResult(val progressMessage: String) : DefaultProgress(progressMessage)
