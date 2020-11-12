package de.rki.coronawarnapp.task.common

import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.util.ui.CachedString
import de.rki.coronawarnapp.util.ui.LazyString

data class DefaultProgress constructor(
    override val primaryMessage: LazyString
) : Task.Progress {

    constructor(
        primary: String
    ) : this(CachedString { primary })
}
