package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemRiskEventBinding

class RiskEventVH(parent: ViewGroup) :
    DayDataAdapter.ItemVH<RiskEventItem, ContactDiaryOverviewDayListItemRiskEventBinding>(
        layoutRes = R.layout.contact_diary_overview_day_list_item_risk_event,
        parent = parent
    ) {

    private val riskEventAdapter: RiskEventAdapter by lazy { RiskEventAdapter() }

    override val viewBinding: Lazy<ContactDiaryOverviewDayListItemRiskEventBinding> =
        lazy { ContactDiaryOverviewDayListItemRiskEventBinding.bind(itemView) }

    override val onBindData: ContactDiaryOverviewDayListItemRiskEventBinding.(item: RiskEventItem, payloads: List<Any>)
    -> Unit =
        { item, _ ->

            with(context) {
                contactDiaryOverviewItemRiskTitle.text = getString(item.title)
                contactDiaryOverviewItemRiskBody.text = getString(item.body)
            }

            contactDiaryOverviewRiskItemImage.setImageResource(item.drawableId)

            with(contactDiaryOverviewItemRiskEventList) {
                if (adapter == null) {
                    adapter = riskEventAdapter
                }
                riskEventAdapter.setItems(item.events)
            }
        }
}
