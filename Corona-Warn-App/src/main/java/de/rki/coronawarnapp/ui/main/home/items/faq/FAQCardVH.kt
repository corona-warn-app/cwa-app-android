package de.rki.coronawarnapp.ui.main.home.items.faq

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeFaqCardLayoutBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.main.home.items.faq.FAQCardVH.Item

class FAQCardVH(parent: ViewGroup) : HomeAdapter.HomeItemVH<Item, HomeFaqCardLayoutBinding>(
    R.layout.home_card_container_layout, parent
) {

    override val viewBinding = lazy {
        HomeFaqCardLayoutBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeFaqCardLayoutBinding.(item: Item) -> Unit = { item ->
        itemView.setOnClickListener { item.onClickAction(item) }
    }

    data class Item(val onClickAction: (Item) -> Unit) : HomeItem {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()
    }
}
