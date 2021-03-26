package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemContactBinding

class ContactVH(parent: ViewGroup) :
    DayDataAdapter.ItemVH<ContactItem, ContactDiaryOverviewDayListItemContactBinding>(
        layoutRes = R.layout.contact_diary_overview_day_list_item_contact,
        parent = parent
    ) {

    private val contactAdapter by lazy { ContactAdapter() }

    override val viewBinding: Lazy<ContactDiaryOverviewDayListItemContactBinding> =
        lazy { ContactDiaryOverviewDayListItemContactBinding.bind(itemView) }

    override val onBindData: ContactDiaryOverviewDayListItemContactBinding.(item: ContactItem, payloads: List<Any>) -> Unit =
        { item, _ ->
            recyclerView.apply {
                adapter = contactAdapter.apply { setItems(item.data) }
                suppressLayout(true)
            }
        }
}
