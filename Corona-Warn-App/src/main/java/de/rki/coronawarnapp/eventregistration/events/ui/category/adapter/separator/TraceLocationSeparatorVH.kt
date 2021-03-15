package de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.separator

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.EventRegistrationCategorySeparatorBinding
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.TraceLocationCategoryAdapter

class TraceLocationSeparatorVH(parent: ViewGroup) :
    TraceLocationCategoryAdapter.ItemVH<TraceLocationSeparatorItem, EventRegistrationCategorySeparatorBinding>(
        layoutRes = R.layout.event_registration_category_separator,
        parent = parent
    ) {

    override val viewBinding: Lazy<EventRegistrationCategorySeparatorBinding> =
        lazy { EventRegistrationCategorySeparatorBinding.bind(itemView) }

    override val onBindData:
        EventRegistrationCategorySeparatorBinding.(item: TraceLocationSeparatorItem, payloads: List<Any>) -> Unit =
            { _, _ ->
                // NOOP
            }
}
