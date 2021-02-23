package de.rki.coronawarnapp.contactdiary.ui.day.tabs.common

import androidx.annotation.StringRes
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.ui.LazyString

abstract class SelectableDiaryItem<T : HasStableId> : HasStableId {
    abstract val onItemClick: (SelectableDiaryItem<T>) -> Unit
    abstract val selected: Boolean
    abstract val item: T
    abstract val contentDescription: LazyString
    abstract val onClickDescription: LazyString
    @get:StringRes abstract val clickLabel: Int
    @get:StringRes abstract val onClickLabel: Int
    override val stableId: Long
        get() = item.stableId
}
