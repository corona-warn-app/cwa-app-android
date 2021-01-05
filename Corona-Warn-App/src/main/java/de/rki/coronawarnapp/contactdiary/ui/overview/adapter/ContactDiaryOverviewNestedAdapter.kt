package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.contactdiary.util.clearAndAddAll
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewNestedListItemBinding

class ContactDiaryOverviewNestedAdapter :
    RecyclerView.Adapter<ContactDiaryOverviewNestedAdapter.NestedItemViewHolder>() {

    private val dataList: MutableList<ListItem.Data> = mutableListOf()

    fun setItems(dataList: List<ListItem.Data>) {
        this.dataList.clearAndAddAll(dataList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return NestedItemViewHolder(ContactDiaryOverviewNestedListItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: NestedItemViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size

    class NestedItemViewHolder(private val viewBinding: ContactDiaryOverviewNestedListItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(data: ListItem.Data) {
            viewBinding.contactDiaryOverviewElementImage.setImageResource(data.drawableId)
            viewBinding.contactDiaryOverviewElementName.text = data.text
        }
    }
}
