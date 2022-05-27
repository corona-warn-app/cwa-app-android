package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewAdapter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactAdapter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest.CoronaTestAdapter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest.CoronaTestItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskenf.RiskEnfItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent.RiskEventAdapter
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent.RiskEventItem
import de.rki.coronawarnapp.contactdiary.util.getLocale
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import de.rki.coronawarnapp.contactdiary.util.toFormattedDayForAccessibility
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemContactBinding
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemHeaderBinding
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemRiskEnfBinding
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemRiskEventBinding
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemTestResultsBinding
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListItemBinding
import java.time.LocalDate

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
                dayHeader.apply(date = date)
                dayRiskEnf.apply(riskEnfItem = riskEnfItem)
                dayRiskEvent.apply(riskEventItem = riskEventItem)
                dayContact.apply(contactItem = contactItem)
                dayTestResult.apply(coronaTestItem = coronaTestItem)
            }

            dayElementBody.apply {
                contentDescription = item.date.toFormattedDayForAccessibility(context.getLocale())
                setOnClickListener { item.onItemSelectionListener(item) }
            }
        }

    private fun ContactDiaryOverviewDayListItemHeaderBinding.apply(date: LocalDate) = this.apply {
        this.date.text = date.toFormattedDay(context.getLocale())
    }

    private fun ContactDiaryOverviewDayListItemRiskEnfBinding.apply(riskEnfItem: RiskEnfItem?) = this.apply {
        root.isGone = riskEnfItem == null

        riskEnfItem?.let {
            contactDiaryOverviewItemRiskTitle.text = context.getString(it.title)
            contactDiaryOverviewRiskItemImage.setImageResource(it.drawableId)

            val sb = StringBuilder().append(context.getString(it.body))

            it.bodyExtended?.let { extend ->
                sb.appendLine().append(context.getString(extend))
            }

            contactDiaryOverviewItemRiskBody.text = sb
        }
    }

    private fun ContactDiaryOverviewDayListItemRiskEventBinding.apply(riskEventItem: RiskEventItem?) {
        root.isGone = riskEventItem == null

        riskEventItem?.let {
            with(context) {
                contactDiaryOverviewItemRiskTitle.text = getString(it.title)
                contactDiaryOverviewItemRiskBody.text = getString(it.body)
            }

            contactDiaryOverviewRiskItemImage.setImageResource(it.drawableId)

            with(contactDiaryOverviewItemRiskEventList) {
                if (adapter == null) {
                    adapter = riskEventAdapter
                }
                riskEventAdapter.setItems(it.events)
            }
        }
    }

    private fun ContactDiaryOverviewDayListItemContactBinding.apply(contactItem: ContactItem?) {
        root.isGone = contactItem == null || contactItem.data.isEmpty()

        contactItem?.let {
            recyclerView.apply {
                adapter = contactAdapter.apply { setItems(it.data) }
                suppressLayout(true)
            }
        }
    }

    private fun ContactDiaryOverviewDayListItemTestResultsBinding.apply(coronaTestItem: CoronaTestItem?) {
        root.isGone = coronaTestItem == null || coronaTestItem.data.isEmpty()

        coronaTestItem?.let {
            recyclerView.apply {
                adapter = CoronaTestAdapter(it.data)
                suppressLayout(true)
            }
        }
    }
}
