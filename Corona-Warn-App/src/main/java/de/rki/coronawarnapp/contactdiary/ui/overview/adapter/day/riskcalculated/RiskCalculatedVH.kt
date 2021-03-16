package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskcalculated

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemRiskCalculatedBinding

class RiskCalculatedVH(parent: ViewGroup) :
    DayDataAdapter.ItemVH<RiskCalculatedItem, ContactDiaryOverviewDayListItemRiskCalculatedBinding>(
        layoutRes = R.layout.contact_diary_overview_day_list_item_risk_calculated,
        parent = parent
    ) {

    override val viewBinding: Lazy<ContactDiaryOverviewDayListItemRiskCalculatedBinding> =
        lazy { ContactDiaryOverviewDayListItemRiskCalculatedBinding.bind(itemView) }

    override val onBindData: ContactDiaryOverviewDayListItemRiskCalculatedBinding.(item: RiskCalculatedItem, payloads: List<Any>) -> Unit =
        { item, _ ->

            contactDiaryOverviewItemRiskTitle.text = context.getString(item.title)
            this.contactDiaryOverviewRiskItemImage.setImageResource(item.drawableId)

            val sb = StringBuilder().append(context.getString(item.body))

            item.bodyExtended?.let { extend ->
                sb.appendLine().append(context.getString(extend))
            }

            this.contactDiaryOverviewItemRiskBody.text = sb
        }
}
