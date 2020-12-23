package de.rki.coronawarnapp.tracing.ui.details.items.behavior

import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem

interface BehaviorItem : DetailsItem {
    override val stableId: Long
        get() = BehaviorItem::class.java.name.hashCode().toLong()
}
