package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListItemBinding
import org.joda.time.LocalDate

class ContactDiaryOverviewAdapter(
    private val dateFormatter: (LocalDate) -> String,
    private val dateFormatterForAccessibility: (LocalDate) -> String,
    private val onItemSelectionListener: (ListItem) -> Unit
) :
    RecyclerView.Adapter<ContactDiaryOverviewAdapter.OverviewElementHolder>() {
    private val elements: MutableList<ListItem> = mutableListOf()

    fun setItems(elements: List<ListItem>) {
        this.elements.clear()
        this.elements += elements
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverviewElementHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OverviewElementHolder(
            ContactDiaryOverviewListItemBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = elements.size

    override fun onBindViewHolder(holder: OverviewElementHolder, position: Int) {
        holder.bind(elements[position], dateFormatter, dateFormatterForAccessibility, onItemSelectionListener)
    }

    class OverviewElementHolder(private val viewDataBinding: ContactDiaryOverviewListItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        private val nestedItemAdapter = ContactDiaryOverviewNestedAdapter()

        init {
            viewDataBinding.contactDiaryOverviewNestedRecyclerView.adapter = nestedItemAdapter
        }

        fun bind(
            item: ListItem,
            dateFormatter: (LocalDate) -> String,
            dateFormatterForAccessibility: (LocalDate) -> String,
            onElementSelectionListener: (ListItem) -> Unit
        ) {
            viewDataBinding.contactDiaryOverviewElementName.text = dateFormatter(item.date)

            viewDataBinding.contactDiaryOverviewElementName.contentDescription =
                dateFormatterForAccessibility(item.date)

            viewDataBinding.contactDiaryOverviewElementBody.setOnClickListener { onElementSelectionListener(item) }

            viewDataBinding.contactDiaryOverviewNestedElementGroup.isGone = item.data.isEmpty()

            nestedItemAdapter.setItems(item.data)
        }
    }
}
