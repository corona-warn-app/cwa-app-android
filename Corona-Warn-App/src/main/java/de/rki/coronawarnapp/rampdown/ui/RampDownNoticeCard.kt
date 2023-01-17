package de.rki.coronawarnapp.rampdown.ui

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeRampDownNoticeCardBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.main.home.rampdown.RampDownNotice
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RampDownNoticeCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<RampDownNoticeCard.Item, HomeRampDownNoticeCardBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeRampDownNoticeCardBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeRampDownNoticeCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
        rampdownCardHeaderHeadline.text = curItem.rampDownNotice.title
        rampdownCardContentBody.text = curItem.rampDownNotice.subtitle
    }

    data class Item(
        val onClickAction: (Item) -> Unit,
        val rampDownNotice: RampDownNotice
    ) : HomeItem, HasPayloadDiffer {
        override val stableId = Item::class.hashCode().toLong()
    }
}
