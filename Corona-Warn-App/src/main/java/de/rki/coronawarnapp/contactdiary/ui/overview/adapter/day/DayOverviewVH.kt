package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewAdapter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactAdapter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest.CoronaTestAdapter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent.RiskEventAdapter
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import de.rki.coronawarnapp.contactdiary.util.toFormattedDayForAccessibility
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListItemBinding

class DayOverviewVH(parent: ViewGroup) :
    DiaryOverviewAdapter.ItemVH<DayOverviewItem, ContactDiaryOverviewListItemBinding>(
        layoutRes = R.layout.contact_diary_overview_list_item,
        parent = parent
    ) {

    private val riskEventAdapter: RiskEventAdapter by lazy { RiskEventAdapter() }
    private val contactAdapter: ContactAdapter by lazy { ContactAdapter() }

    override val viewBinding: Lazy<ContactDiaryOverviewListItemBinding> =
        lazy { ContactDiaryOverviewListItemBinding.bind(itemView) }

    override val onBindData: ContactDiaryOverviewListItemBinding.(item: DayOverviewItem, payloads: List<Any>) -> Unit =
        { item, _ ->

            item.apply {
                headerDate.text = date.toFormattedDay(context.getLocale())
                dayRiskEnf.isGone = riskEnfItem == null
                riskEnfItem?.let {
                    contactDiaryOverviewItemRiskTitle.text = context.getString(it.title)
                    contactDiaryOverviewRiskItemImage.setImageResource(it.drawableId)

                    val sb = StringBuilder().append(context.getString(it.body))

                    it.bodyExtended?.let { extend ->
                        sb.appendLine().append(context.getString(extend))
                    }

                    contactDiaryOverviewItemRiskBody.text = sb
                }

                dayRiskEvent.isGone = riskEventItem == null
                riskEventItem?.let {
                    with(context) {
                        riskEventItemTitle.text = getString(it.title)
                        riskEventItemBody.text = getString(it.body)
                    }

                    riskEventItemImage.setImageResource(it.drawableId)

                    with(riskEventItemList) {
                        if (adapter == null) {
                            adapter = riskEventAdapter
                        }
                        riskEventAdapter.setItems(it.events)
                    }
                }

                dayContact.isGone = contactItem == null || contactItem.data.isEmpty()

                contactItem?.let {
                    daySubmissionRecyclerView.apply {
                        adapter = contactAdapter.apply { setItems(it.data) }
                        suppressLayout(true)
                    }
                }

                dayTestResult.isGone = coronaTestItem == null || coronaTestItem.data.isEmpty()
                coronaTestItem?.let {
                    recyclerView.apply {
                        adapter = CoronaTestAdapter(it.data)
                        suppressLayout(true)
                    }
                }

                daySubmission.isGone = submissionItem == null
                daySubmission.isGone = submissionItem == null
            }

            dayElementBody.apply {
                contentDescription = item.date.toFormattedDayForAccessibility(context.getLocale())
                setOnClickListener { item.onItemSelectionListener(item) }
            }
        }
}
