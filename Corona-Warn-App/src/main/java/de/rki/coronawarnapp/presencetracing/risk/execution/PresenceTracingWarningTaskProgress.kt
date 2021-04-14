package de.rki.coronawarnapp.presencetracing.risk.execution

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

sealed class PresenceTracingWarningTaskProgress : Task.Progress {

    data class Downloading(
        override val primaryMessage: LazyString = CachedString { "" }
    ) : PresenceTracingWarningTaskProgress()

    data class Calculating(
        override val primaryMessage: LazyString = CachedString { "" }
    ) : PresenceTracingWarningTaskProgress()
}
