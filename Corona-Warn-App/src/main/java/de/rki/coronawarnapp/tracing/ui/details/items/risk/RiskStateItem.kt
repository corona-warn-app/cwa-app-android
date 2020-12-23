package de.rki.coronawarnapp.tracing.ui.details.items.risk

import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem

interface RiskStateItem : DetailsItem {
    override val stableId: Long
        get() = RiskStateItem::class.java.name.hashCode().toLong()
}
