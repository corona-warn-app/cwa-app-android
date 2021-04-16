package de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.separator

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerCategorySeparatorBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.TraceLocationCategoryAdapter

class TraceLocationSeparatorVH(parent: ViewGroup) :
    TraceLocationCategoryAdapter.ItemVH<TraceLocationSeparatorItem, TraceLocationOrganizerCategorySeparatorBinding>(
        layoutRes = R.layout.trace_location_organizer_category_separator,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationOrganizerCategorySeparatorBinding> =
        lazy { TraceLocationOrganizerCategorySeparatorBinding.bind(itemView) }

    override val onBindData:
        TraceLocationOrganizerCategorySeparatorBinding.(item: TraceLocationSeparatorItem, payloads: List<Any>) -> Unit =
            { _, _ ->
                // NOOP
            }
}
