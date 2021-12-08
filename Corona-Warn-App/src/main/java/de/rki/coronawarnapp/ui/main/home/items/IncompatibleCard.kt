package de.rki.coronawarnapp.ui.main.home.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeIncompatibleCardLayoutBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class IncompatibleCard(parent: ViewGroup) :
    HomeAdapter.HomeItemVH<IncompatibleCard.Item, HomeIncompatibleCardLayoutBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeIncompatibleCardLayoutBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeIncompatibleCardLayoutBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        when (item.bluetoothSupported) {
            true ->
                mainCardContentBody.setText(R.string.incompatible_advertising_not_supported)
            false ->
                mainCardContentBody.setText(R.string.incompatible_scanning_not_supported)
        }

        itemView.setOnClickListener {
            val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
            curItem.onClickAction(item)
        }
    }

    data class Item(val onClickAction: (Item) -> Unit, val bluetoothSupported: Boolean) : HomeItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()
    }
}
