package de.rki.coronawarnapp.ui.tracing.details.items.additionalinfos

import de.rki.coronawarnapp.ui.tracing.details.items.DetailsItem

interface AdditionalInformationItem : DetailsItem {
    override val stableId: Long
        get() = AdditionalInformationItem::class.java.name.hashCode().toLong()
}
