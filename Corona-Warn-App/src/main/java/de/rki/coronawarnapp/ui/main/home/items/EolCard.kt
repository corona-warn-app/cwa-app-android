package de.rki.coronawarnapp.ui.main.home.items

import android.graphics.Paint
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeEolCardLayoutBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class EolCard(parent: ViewGroup) :
    HomeAdapter.HomeItemVH<EolCard.Item, HomeEolCardLayoutBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeEolCardLayoutBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeEolCardLayoutBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        eolLink.paintFlags = eolLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        eolLink.setOnClickListener {
            curItem.openEolLink()
        }
        eolText.movementMethod = LinkMovementMethod.getInstance()
    }

    data class Item(val openEolLink: () -> Unit) : HomeItem {
        override val stableId: Long
            get() = Item::class.hashCode().toLong()
    }
}
