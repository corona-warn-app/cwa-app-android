package de.rki.coronawarnapp.familytest.ui.homecard

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FamilyTestCardBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyTestCard(parent: ViewGroup) : HomeAdapter.HomeItemVH<FamilyTestCard.Item, FamilyTestCardBinding>(
    R.layout.home_card_container_layout,
    parent
) {
    override val viewBinding = lazy {
        FamilyTestCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyTestCardBinding.(item: Item, payloads: List<Any>) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        familyBadgeCount.isVisible = curItem.badgeCount != 0
        familyBadgeCount.text = curItem.badgeCount.toString()
        familyTestSubtitleNews.isVisible = curItem.badgeCount != 0
        itemView.setOnClickListener { curItem.onCLickAction() }
    }

    data class Item(
        val badgeCount: Int,
        val onCLickAction: () -> Unit
    ) : HomeItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()
    }
}
