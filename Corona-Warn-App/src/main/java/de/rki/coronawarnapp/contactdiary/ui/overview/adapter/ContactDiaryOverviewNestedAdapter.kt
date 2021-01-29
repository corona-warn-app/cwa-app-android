package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.clearAndAddAll
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewNestedListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

class ContactDiaryOverviewNestedAdapter(
    private val element: ListItem,
    private val onItemSelectionListener: (ListItem) -> Unit
) : BaseAdapter<ContactDiaryOverviewNestedAdapter.NestedItemViewHolder>() {

    private val dataList: MutableList<ListItem.Data> = mutableListOf()

    fun setItems(dataList: List<ListItem.Data>) {
        this.dataList.clearAndAddAll(dataList)
        notifyDataSetChanged()
    }

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): NestedItemViewHolder = NestedItemViewHolder(parent)

    override fun onBindBaseVH(holder: NestedItemViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(dataList[position], payloads)
    }

    override fun getItemCount(): Int = dataList.size

    inner class NestedItemViewHolder(parent: ViewGroup) :
        BaseAdapter.VH(R.layout.contact_diary_overview_nested_list_item, parent),
        BindableVH<ListItem.Data, ContactDiaryOverviewNestedListItemBinding> {
        override val viewBinding:
            Lazy<ContactDiaryOverviewNestedListItemBinding> =
            lazy { ContactDiaryOverviewNestedListItemBinding.bind(itemView) }

        override val onBindData:
            ContactDiaryOverviewNestedListItemBinding.(item: ListItem.Data, payloads: List<Any>) -> Unit =
            { key, _ ->
                contactDiaryOverviewElementImage.setImageResource(key.drawableId)
                contactDiaryOverviewElementName.text = key.text
                contactDiaryOverviewElementName.contentDescription = when (key.type) {
                    ListItem.Type.LOCATION -> context.getString(R.string.accessibility_location, key.text)
                    ListItem.Type.PERSON -> context.getString(R.string.accessibility_person, key.text)
                    else -> key.text
                }

                contactDiaryOverviewElementNestedContainer.setOnClickListener { onItemSelectionListener(element) }
            }
    }
}
