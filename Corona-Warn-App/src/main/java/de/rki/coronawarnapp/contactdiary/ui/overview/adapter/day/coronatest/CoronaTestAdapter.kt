package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewDayListItemTestResultBinding
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.coronatest.CoronaTestAdapter.ViewHolder

class CoronaTestAdapter : ListAdapter<CoronaTestItem.Data, ViewHolder>(ITEM_COMPARATOR) {

    class ViewHolder(private val binding: ContactDiaryOverviewDayListItemTestResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(coronaTestItem: CoronaTestItem.Data) {
            with(binding) {
                contactDiaryCoronaTestTitle.text = root.context.getString(coronaTestItem.header)
                contactDiaryCoronaTestImage.setImageResource(coronaTestItem.icon)
                contactDiaryCoronaTestBody.text = root.context.getString(coronaTestItem.body)
            }
        }
    }

    companion object {
        private val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<CoronaTestItem.Data>() {
            override fun areItemsTheSame(oldItem: CoronaTestItem.Data, newItem: CoronaTestItem.Data) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: CoronaTestItem.Data, newItem: CoronaTestItem.Data) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ContactDiaryOverviewDayListItemTestResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
