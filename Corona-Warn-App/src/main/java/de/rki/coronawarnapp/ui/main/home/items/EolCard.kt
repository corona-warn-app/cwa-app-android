package de.rki.coronawarnapp.ui.main.home.items

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
    ) -> Unit = { _, _ -> }

    class Item : HomeItem {
        override val stableId: Long
            get() = Item::class.hashCode().toLong()
    }
}
