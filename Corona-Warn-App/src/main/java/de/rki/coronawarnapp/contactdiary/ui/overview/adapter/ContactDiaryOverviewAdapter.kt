package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.databinding.IncludeContactDiaryOverviewItemBinding

class ContactDiaryOverviewAdapter(private val onItemSelectionListener: (ListItem) -> Unit) :
    RecyclerView.Adapter<ContactDiaryOverviewAdapter.OverviewElementHolder>() {
    private var _elements = emptyList<ListItem>()

    fun setItems(elements: List<ListItem>) {
        _elements = elements
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverviewElementHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OverviewElementHolder(
            IncludeContactDiaryOverviewItemBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = _elements.size

    override fun onBindViewHolder(holder: OverviewElementHolder, position: Int) {
        holder.bind(_elements[position], onItemSelectionListener)
    }

    class OverviewElementHolder(private val viewDataBinding: IncludeContactDiaryOverviewItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        fun bind(
            item: ListItem,
            onElementSelectionListener: (ListItem) -> Unit
        ) {
            viewDataBinding.executePendingBindings()
        }
    }
}
