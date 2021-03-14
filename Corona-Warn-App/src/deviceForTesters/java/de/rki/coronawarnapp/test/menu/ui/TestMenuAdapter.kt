package de.rki.coronawarnapp.test.menu.ui

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestMenuAdapterItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import javax.inject.Inject

class TestMenuAdapter @Inject constructor() : BaseAdapter<TestMenuAdapter.VH>() {

    private val internalData = mutableListOf<TestMenuItem>()

    var data: List<TestMenuItem>
        get() = internalData.toList()
        set(value) {
            internalData.clear()
            internalData.addAll(value)
            notifyDataSetChanged()
        }

    var onItemClickListener: (TestMenuItem) -> Unit = {}

    override fun getItemCount(): Int = internalData.size

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindBaseVH(holder: VH, position: Int, payloads: MutableList<Any>) {
        holder.apply {
            val item = internalData[position]
            bind(item)
            itemView.setOnClickListener { onItemClickListener(item) }
        }
    }

    class VH(parent: ViewGroup) : BaseAdapter.VH(R.layout.fragment_test_menu_adapter_item, parent) {
        private val viewBinding: FragmentTestMenuAdapterItemBinding =
            FragmentTestMenuAdapterItemBinding.bind(itemView)

        fun bind(item: TestMenuItem) = viewBinding.apply {
            imageView.setImageResource(item.iconRes)
            title.text = item.title
            description.text = item.description
        }
    }
}
