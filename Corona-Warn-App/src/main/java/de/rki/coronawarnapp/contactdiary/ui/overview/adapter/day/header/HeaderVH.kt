package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.header

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataAdapter
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemHeaderBinding

class HeaderVH(parent: ViewGroup) :
    DayDataAdapter.ItemVH<HeaderItem, ContactDiaryOverviewDayListItemHeaderBinding>(
        layoutRes = R.layout.contact_diary_overview_day_list_item_header,
        parent = parent
    ) {

    override val viewBinding: Lazy<ContactDiaryOverviewDayListItemHeaderBinding> =
        lazy { ContactDiaryOverviewDayListItemHeaderBinding.bind(itemView) }

    override val onBindData: ContactDiaryOverviewDayListItemHeaderBinding.(item: HeaderItem, payloads: List<Any>) -> Unit =
        { item, _ ->
            name.text = item.date.toFormattedDay(context.getLocale())
            headerBody.setOnClickListener { item.onclickListener(item) }
        }
}
