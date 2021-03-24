package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.clearAndAddAll
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemRiskEventListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.lists.BindableVH

class RiskEventAdapter : BaseAdapter<RiskEventAdapter.RiskEventListItemVH>() {

    private val events: MutableList<RiskEventItem.Event> = mutableListOf()

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): RiskEventListItemVH = RiskEventListItemVH(parent)

    override fun onBindBaseVH(holder: RiskEventListItemVH, position: Int, payloads: MutableList<Any>) =
        holder.bind(events[position], payloads)

    override fun getItemCount(): Int = events.size

    fun setItems(events: List<RiskEventItem.Event>) {
        this.events.clearAndAddAll(events)
        notifyDataSetChanged()
    }

    inner class RiskEventListItemVH(parent: ViewGroup) :
        BaseAdapter.VH(R.layout.contact_diary_overview_day_list_item_risk_event_list_item, parent),
        BindableVH<RiskEventItem.Event, ContactDiaryOverviewDayListItemRiskEventListItemBinding> {

        override val viewBinding: Lazy<ContactDiaryOverviewDayListItemRiskEventListItemBinding> =
            lazy { ContactDiaryOverviewDayListItemRiskEventListItemBinding.bind(itemView) }

        override val onBindData: ContactDiaryOverviewDayListItemRiskEventListItemBinding.(item: RiskEventItem.Event, payloads: List<Any>) -> Unit =
            { item, _ ->

                bulletPointImage.drawable?.setTint(context.getColorCompat(item.bulledPointColor))

                var name = item.name

                item.riskInfoAddition?.let {
                    name += " (${context.getString(it)})"
                }

                eventName.text = name
            }
    }
}
