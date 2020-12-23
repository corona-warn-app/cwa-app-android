package de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos

import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem

interface AdditionalInformationItem : DetailsItem {
    override val stableId: Long
        get() = AdditionalInformationItem::class.java.name.hashCode().toLong()
}
