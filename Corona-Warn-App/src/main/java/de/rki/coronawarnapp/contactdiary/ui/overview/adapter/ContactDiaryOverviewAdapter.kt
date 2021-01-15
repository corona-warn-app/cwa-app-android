package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.clearAndAddAll
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import org.joda.time.LocalDate

class ContactDiaryOverviewAdapter(
    private val dateFormatter: (LocalDate) -> String,
    private val dateFormatterForAccessibility: (LocalDate) -> String,
    private val onItemSelectionListener: (ListItem) -> Unit
) : BaseAdapter<ContactDiaryOverviewAdapter.OverviewElementHolder>() {

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

        override val onBindData: ContactDiaryOverviewListItemBinding.(item: ListItem, payloads: List<Any>) -> Unit =
            { item, _ ->
                val nestedItemAdapter = ContactDiaryOverviewNestedAdapter(item, onItemSelectionListener)
                viewBinding.value.contactDiaryOverviewNestedRecyclerView.adapter = nestedItemAdapter
                contactDiaryOverviewElementName.text = dateFormatter(item.date)
                contactDiaryOverviewElementName.contentDescription = dateFormatterForAccessibility(item.date)
                contactDiaryOverviewElementBody.setOnClickListener { onItemSelectionListener(item) }
                contactDiaryOverviewNestedElementGroup.isGone = item.data.isEmpty()
                nestedItemAdapter.setItems(item.data)
            }
    }
}
