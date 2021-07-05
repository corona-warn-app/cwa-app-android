package de.rki.coronawarnapp.statistics.ui.stateselection

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FederalStateInputAdapterItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter

class FederalStateAdapter constructor(val onItemClickListener: (ListItem) -> Unit) :
    BaseAdapter<FederalStateAdapter.VH>() {

    private val internalData = mutableListOf<ListItem>()

    var data: List<ListItem>
        get() = internalData.toList()
        set(value) {
            internalData.clear()
            internalData.addAll(value)
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = internalData.size

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindBaseVH(holder: VH, position: Int, payloads: MutableList<Any>) {
        holder.apply {
            val item = internalData[position]
            bind(item)
            itemView.setOnClickListener { onItemClickListener(item) }
        }
    }

    class VH(parent: ViewGroup) : BaseAdapter.VH(R.layout.federal_state_input_adapter_item, parent) {
        private val viewBinding: FederalStateInputAdapterItemBinding =
            FederalStateInputAdapterItemBinding.bind(itemView)

        fun bind(item: ListItem) = viewBinding.apply {
            label.text = item.label.get(context)
        }
    }
}
