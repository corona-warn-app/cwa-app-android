package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewAdapter
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.toFormattedDayForAccessibility
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.update

class DayOverviewVH(parent: ViewGroup) :
    DiaryOverviewAdapter.ItemVH<DayOverviewItem, ContactDiaryOverviewListItemBinding>(
        layoutRes = R.layout.contact_diary_overview_list_item,
        parent = parent
    ) {

    private val dayDataAdapter by lazy { DayDataAdapter() }

    override val viewBinding: Lazy<ContactDiaryOverviewListItemBinding> =
        lazy { ContactDiaryOverviewListItemBinding.bind(itemView) }

    override val onBindData: ContactDiaryOverviewListItemBinding.(item: DayOverviewItem, payloads: List<Any>) -> Unit =
        { item, _ ->
            dayRecyclerView.apply {
                adapter = dayDataAdapter.apply { update(item.dayData) }
                itemAnimator = null
            }

            dayElementBody.apply {
                contentDescription = item.date.toFormattedDayForAccessibility(context.getLocale())
                setOnClickListener { item.onItemSelectionListener(item) }
            }
        }
}

