package de.rki.coronawarnapp.tracing.ui.details.items.riskdetails

import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem

interface RiskDetailsStateItem : DetailsItem {
    override val stableId: Long
        get() = RiskDetailsStateItem::class.java.name.hashCode().toLong()
}
