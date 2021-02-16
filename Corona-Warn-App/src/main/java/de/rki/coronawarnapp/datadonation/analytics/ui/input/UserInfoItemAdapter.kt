package de.rki.coronawarnapp.datadonation.analytics.ui.input

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.AnalyticsPpaUserinfoInputAdapterItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import javax.inject.Inject

class UserInfoItemAdapter @Inject constructor() : BaseAdapter<UserInfoItemAdapter.VH>() {

    private val internalData = mutableListOf<UserInfoItem>()

    var data: List<UserInfoItem>
        get() = internalData.toList()
        set(value) {
            internalData.clear()
            internalData.addAll(value)
            notifyDataSetChanged()
        }

    var onItemClickListener: (UserInfoItem) -> Unit = {}

    override fun getItemCount(): Int = internalData.size

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindBaseVH(holder: VH, position: Int, payloads: MutableList<Any>) {
        holder.apply {
            val item = internalData[position]
            bind(item)
            itemView.setOnClickListener { onItemClickListener(item) }
        }
    }

    class VH(parent: ViewGroup) : BaseAdapter.VH(R.layout.analytics_ppa_userinfo_input_adapter_item, parent) {
        private val viewBinding: AnalyticsPpaUserinfoInputAdapterItemBinding =
            AnalyticsPpaUserinfoInputAdapterItemBinding.bind(itemView)

        fun bind(item: UserInfoItem) = viewBinding.apply {
            label.text = item.label.get(context)
            radiobutton.isChecked = item.isSelected
        }
    }
}
