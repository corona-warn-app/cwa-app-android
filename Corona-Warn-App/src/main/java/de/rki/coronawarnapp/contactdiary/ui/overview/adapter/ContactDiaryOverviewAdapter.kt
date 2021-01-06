package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.clearAndAddAll
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

class ContactDiaryOverviewAdapter(private val onItemSelectionListener: (ListItem) -> Unit) :
    BaseAdapter<ContactDiaryOverviewAdapter.OverviewElementHolder>() {
    private val elements: MutableList<ListItem> = mutableListOf()

    fun setItems(elements: List<ListItem>) {
        this.elements.clearAndAddAll(elements)
        notifyDataSetChanged()
    }

    override fun getItemCount() = elements.size

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): OverviewElementHolder = OverviewElementHolder(parent)

    override fun onBindBaseVH(holder: OverviewElementHolder, position: Int, payloads: MutableList<Any>) =
        holder.bind(elements[position], payloads)

    inner class OverviewElementHolder(parent: ViewGroup) :
        BaseAdapter.VH(R.layout.contact_diary_overview_list_item, parent),
        BindableVH<ListItem, ContactDiaryOverviewListItemBinding> {
        override val viewBinding: Lazy<ContactDiaryOverviewListItemBinding> =
            lazy { ContactDiaryOverviewListItemBinding.bind(itemView) }

        private val nestedItemAdapter = ContactDiaryOverviewNestedAdapter()

        init {
            viewBinding.value.contactDiaryOverviewNestedRecyclerView.adapter = nestedItemAdapter
        }

        override val onBindData: ContactDiaryOverviewListItemBinding.(item: ListItem, payloads: List<Any>) -> Unit =
            { key, _ ->
                contactDiaryOverviewElementName.text = key.date.toFormattedDay()
                contactDiaryOverviewElementBody.setOnClickListener { onItemSelectionListener(key) }
                contactDiaryOverviewNestedElementGroup.isGone = key.data.isEmpty()
                nestedItemAdapter.setItems(key.data)
            }
    }
}
