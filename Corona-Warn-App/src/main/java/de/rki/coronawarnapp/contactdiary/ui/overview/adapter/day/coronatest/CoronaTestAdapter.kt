package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest.CoronaTestAdapter.ViewHolder
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemTestResultBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

class CoronaTestAdapter(val items: List<CoronaTestItem.Data>) : BaseAdapter<ViewHolder>() {

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindBaseVH(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(items[position], payloads)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(parent: ViewGroup) :
        VH(R.layout.contact_diary_overview_day_list_item_test_result, parent),
        BindableVH<CoronaTestItem.Data, ContactDiaryOverviewDayListItemTestResultBinding> {

        override val viewBinding: Lazy<ContactDiaryOverviewDayListItemTestResultBinding> =
            lazy { ContactDiaryOverviewDayListItemTestResultBinding.bind(itemView) }

        override val onBindData: ContactDiaryOverviewDayListItemTestResultBinding.(
            item: CoronaTestItem.Data,
            payloads: List<Any>
        ) -> Unit
            get() = { coronaTestItem, _ ->
                with(root.context) {
                    contactDiaryCoronaTestTitle.text = getString(coronaTestItem.header)
                    contactDiaryCoronaTestImage.setImageResource(coronaTestItem.icon)
                    contactDiaryCoronaTestBody.text = getString(coronaTestItem.body)
                }
            }
    }
}
