package de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.header

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.EventRegistrationCategoryHeaderBinding
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.TraceLocationCategoryAdapter

class TraceLocationHeaderVH(parent: ViewGroup) :
    TraceLocationCategoryAdapter.ItemVH<TraceLocationHeaderItem, EventRegistrationCategoryHeaderBinding>(
        layoutRes = R.layout.event_registration_category_header,
        parent = parent
    ) {
    override val viewBinding: Lazy<EventRegistrationCategoryHeaderBinding> =
        lazy { EventRegistrationCategoryHeaderBinding.bind(itemView) }

    override val onBindData:
        EventRegistrationCategoryHeaderBinding.(item: TraceLocationHeaderItem, payloads: List<Any>) -> Unit =
            { item, _ ->
                categoryHeader.text = context.getString(item.headerText)
            }
}
