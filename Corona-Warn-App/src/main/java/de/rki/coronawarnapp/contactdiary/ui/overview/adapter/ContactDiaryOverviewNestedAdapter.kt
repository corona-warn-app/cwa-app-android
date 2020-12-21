package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.databinding.IncludeContactDiaryOverviewNestedItemBinding

class ContactDiaryOverviewNestedAdapter(
    private val element: ListItem,
    private val onItemSelectionListener: (ListItem) -> Unit
) : RecyclerView.Adapter<ContactDiaryOverviewNestedAdapter.NestedItemViewHolder>() {

    private val dataList: MutableList<ListItem.Data> = mutableListOf()

    fun setItems(dataList: List<ListItem.Data>) {
        this.dataList.clear()
        this.dataList += dataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return NestedItemViewHolder(IncludeContactDiaryOverviewNestedItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: NestedItemViewHolder, position: Int) {
        holder.bind(dataList[position], element, onItemSelectionListener)
    }

    override fun getItemCount(): Int = dataList.size

    class NestedItemViewHolder(private val viewBinding: IncludeContactDiaryOverviewNestedItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(data: ListItem.Data, item: ListItem, selectedItem: (ListItem) -> Unit) {
            viewBinding.contactDiaryOverviewElementImage.setImageResource(data.drawableId)
            viewBinding.contactDiaryOverviewElementName.text = data.text
            viewBinding.contactDiaryOverviewElementNestedContainer.setOnClickListener { selectedItem(item) }
        }
    }
}
