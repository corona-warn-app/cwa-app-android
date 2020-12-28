package de.rki.coronawarnapp.contactdiary.util

import de.rki.coronawarnapp.util.lists.HasStableId

data class SelectableItem<T : HasStableId>(
    val selected: Boolean,
    val item: T,
    val contentDescription: String,
    val onClickDescription: String,
    val clickLabel: String,
    val onClickLabel: String,
    override val stableId: Long = item.stableId
) : HasStableId
