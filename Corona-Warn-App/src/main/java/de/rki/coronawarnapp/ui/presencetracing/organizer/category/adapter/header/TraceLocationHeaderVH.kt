package de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.header

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerCategoryHeaderBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.TraceLocationCategoryAdapter

class TraceLocationHeaderVH(parent: ViewGroup) :
    TraceLocationCategoryAdapter.ItemVH<TraceLocationHeaderItem, TraceLocationOrganizerCategoryHeaderBinding>(
        layoutRes = R.layout.trace_location_organizer_category_header,
        parent = parent
    ) {
    override val viewBinding: Lazy<TraceLocationOrganizerCategoryHeaderBinding> =
        lazy { TraceLocationOrganizerCategoryHeaderBinding.bind(itemView) }

    override val onBindData:
        TraceLocationOrganizerCategoryHeaderBinding.(item: TraceLocationHeaderItem, payloads: List<Any>) -> Unit =
            { item, _ ->
                categoryHeader.text = context.getString(item.headerText)
            }
}
