package de.rki.coronawarnapp.contactdiary.util

import androidx.annotation.StringRes
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.ui.LazyString

data class SelectableItem<T : HasStableId>(
    val selected: Boolean,
    val item: T,
    val contentDescription: LazyString,
    val onClickDescription: LazyString,
    @StringRes val clickLabel: Int,
    @StringRes val onClickLabel: Int,
    override val stableId: Long = item.stableId
) : HasStableId
