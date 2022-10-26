package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskevent

import android.view.ViewGroup
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemRiskEventListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.lists.BindableVH

class RiskEventAdapter : BaseAdapter<RiskEventAdapter.RiskEventListItemVH>() {

    private val events: SortedList<RiskEventItem.Event> = SortedList(
        RiskEventItem.Event::class.java,
        SortedList.BatchedCallback(
            object : SortedListAdapterCallback<RiskEventItem.Event>(
                this
            ) {
                override fun compare(o1: RiskEventItem.Event, o2: RiskEventItem.Event): Int =
                    o1.description.compareTo(o2.description)

                override fun areContentsTheSame(oldItem: RiskEventItem.Event?, newItem: RiskEventItem.Event?): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(item1: RiskEventItem.Event?, item2: RiskEventItem.Event?): Boolean =
                    item1 == item2
            }
        )
    )

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): RiskEventListItemVH = RiskEventListItemVH(parent)

    override fun onBindBaseVH(holder: RiskEventListItemVH, position: Int, payloads: MutableList<Any>) =
        holder.bind(events[position], payloads)

    override fun getItemCount(): Int = events.size()

    fun setItems(events: List<RiskEventItem.Event>) {
        this.events.apply {
            beginBatchedUpdates()
            clear()
            addAll(events)
            endBatchedUpdates()
        }
    }

    inner class RiskEventListItemVH(parent: ViewGroup) :
        VH(R.layout.contact_diary_overview_day_list_item_risk_event_list_item, parent),
        BindableVH<RiskEventItem.Event, ContactDiaryOverviewDayListItemRiskEventListItemBinding> {

        override val viewBinding:
            Lazy<ContactDiaryOverviewDayListItemRiskEventListItemBinding> =
                lazy { ContactDiaryOverviewDayListItemRiskEventListItemBinding.bind(itemView) }

        override val onBindData:
            ContactDiaryOverviewDayListItemRiskEventListItemBinding.(item: RiskEventItem.Event, payloads: List<Any>)
            -> Unit =
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
