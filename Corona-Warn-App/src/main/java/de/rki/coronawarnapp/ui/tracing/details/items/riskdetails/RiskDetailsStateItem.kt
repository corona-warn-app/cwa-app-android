package de.rki.coronawarnapp.ui.tracing.details.items.riskdetails

import de.rki.coronawarnapp.ui.tracing.details.items.DetailsItem

interface RiskDetailsStateItem : DetailsItem {
    override val stableId: Long
        get() = RiskDetailsStateItem::class.java.name.hashCode().toLong()
}
