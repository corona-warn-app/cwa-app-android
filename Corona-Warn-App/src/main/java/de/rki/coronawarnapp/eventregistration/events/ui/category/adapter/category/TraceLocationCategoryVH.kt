package de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.category

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.EventRegistrationCategoryItemBinding
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.TraceLocationCategoryAdapter

class TraceLocationCategoryVH(parent: ViewGroup, onItemClickListener: (category: TraceLocationCategory) -> Unit) :
    TraceLocationCategoryAdapter.ItemVH<TraceLocationCategory, EventRegistrationCategoryItemBinding>(
        layoutRes = R.layout.event_registration_category_item,
        parent = parent
    ) {

    override val viewBinding: Lazy<EventRegistrationCategoryItemBinding> = lazy {
        EventRegistrationCategoryItemBinding.bind(itemView)
    }

    override val onBindData:
        EventRegistrationCategoryItemBinding.(item: TraceLocationCategory, payloads: List<Any>) -> Unit = { item, _ ->
            title.text = context.getString(item.title)
            if (item.subtitle != null) {
                subtitle.text = context.getString(item.subtitle)
            }
            root.setOnClickListener { onItemClickListener.invoke(item) }
        }
}
