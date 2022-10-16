package de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerCategoryItemBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.TraceLocationCategoryAdapter

class TraceLocationCategoryVH(parent: ViewGroup, onItemClickListener: (category: TraceLocationCategory) -> Unit) :
    TraceLocationCategoryAdapter.ItemVH<TraceLocationCategory, TraceLocationOrganizerCategoryItemBinding>(
        layoutRes = R.layout.trace_location_organizer_category_item,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationOrganizerCategoryItemBinding> = lazy {
        TraceLocationOrganizerCategoryItemBinding.bind(itemView)
    }

    override val onBindData:
        TraceLocationOrganizerCategoryItemBinding.(item: TraceLocationCategory, payloads: List<Any>) -> Unit =
            { item, _ ->
                categoryItemTitle.text = context.getString(item.title)
                if (item.subtitle != null) {
                    categoryItemSubtitle.text = context.getString(item.subtitle)
                }
                root.setOnClickListener { onItemClickListener.invoke(item) }
            }
}
