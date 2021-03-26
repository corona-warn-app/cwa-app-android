package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskenf

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayDataAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemRiskEnfBinding

class RiskEnfVH(parent: ViewGroup) :
    DayDataAdapter.ItemVH<RiskEnfItem, ContactDiaryOverviewDayListItemRiskEnfBinding>(
        layoutRes = R.layout.contact_diary_overview_day_list_item_risk_enf,
        parent = parent
    ) {

    override val viewBinding:
        Lazy<ContactDiaryOverviewDayListItemRiskEnfBinding> =
            lazy { ContactDiaryOverviewDayListItemRiskEnfBinding.bind(itemView) }

    override val onBindData:
        ContactDiaryOverviewDayListItemRiskEnfBinding.(item: RiskEnfItem, payloads: List<Any>) -> Unit =
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
