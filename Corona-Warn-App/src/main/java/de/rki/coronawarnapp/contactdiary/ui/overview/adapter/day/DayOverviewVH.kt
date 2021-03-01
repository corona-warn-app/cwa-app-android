package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewAdapter
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import de.rki.coronawarnapp.contactdiary.util.toFormattedDayForAccessibility
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListItemBinding

class DayOverviewVH(parent: ViewGroup) :
    DiaryOverviewAdapter.ItemVH<DayOverviewItem, ContactDiaryOverviewListItemBinding>(
        layoutRes = R.layout.contact_diary_overview_list_item,
        parent = parent
    ) {

    private val nestedItemAdapter by lazy { DayDataNestedAdapter() }

    override val viewBinding: Lazy<ContactDiaryOverviewListItemBinding> =
        lazy { ContactDiaryOverviewListItemBinding.bind(itemView) }

    override val onBindData: ContactDiaryOverviewListItemBinding.(item: DayOverviewItem, payloads: List<Any>) -> Unit =
        { item, _ ->
            contactDiaryOverviewNestedRecyclerView.adapter = nestedItemAdapter
            contactDiaryOverviewNestedRecyclerView.suppressLayout(true)
            contactDiaryOverviewElementBody.setOnClickListener { item.onItemSelectionListener(item) }

            contactDiaryOverviewElementBody.contentDescription =
                item.date.toFormattedDayForAccessibility(context.getLocale())

            contactDiaryOverviewElementName.apply {
                text = item.date.toFormattedDay(context.getLocale())
            }

            contactDiaryOverviewNestedElementGroup.isGone = item.data.isEmpty()
            nestedItemAdapter.setItems(item.data)

            contactDiaryOverviewNestedListItemRisk.apply {
                item.risk?.let {
                    this.contactDiaryOverviewRiskItem.isGone = false
                    this.contactDiaryOverviewItemRiskTitle.text = context.getString(it.title)
                    this.contactDiaryOverviewRiskItemImage.setImageResource(it.drawableId)

                    val sb = StringBuilder().append(context.getString(it.body))

                    it.bodyExtended?.let { extend ->
                        sb.appendLine().append(context.getString(extend))
                    }

                    this.contactDiaryOverviewItemRiskBody.text = sb
                } ?: run { this.contactDiaryOverviewRiskItem.isGone = true }
            }
        }
}
