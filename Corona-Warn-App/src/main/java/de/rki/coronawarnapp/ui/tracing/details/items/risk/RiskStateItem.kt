package de.rki.coronawarnapp.ui.tracing.details.items.risk

import de.rki.coronawarnapp.ui.tracing.details.items.DetailsItem

interface RiskStateItem : DetailsItem {
    override val stableId: Long
        get() = RiskStateItem::class.java.name.hashCode().toLong()
}
