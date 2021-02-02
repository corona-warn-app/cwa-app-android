package de.rki.coronawarnapp.ui.main.home.items

import android.view.ViewGroup
import android.widget.Button
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeReenableRiskCardLayoutBinding
import de.rki.coronawarnapp.submission.ui.homecards.SubmissionDone
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.ReenableRiskCard.Item
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class ReenableRiskCard(parent: ViewGroup) : HomeAdapter.HomeItemVH<Item, HomeReenableRiskCardLayoutBinding>(
    R.layout.home_card_container_layout, parent
) {

    override val viewBinding = lazy {
        HomeReenableRiskCardLayoutBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeReenableRiskCardLayoutBinding.(Item, List<Any>) -> Unit = { item, payloads ->

        state = item.state
        itemView.findViewById<Button>(R.id.reenable_risk_card_button).setOnClickListener {
            val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
            curItem.onClickAction(item)
        }
    }

    data class Item(val state: SubmissionDone, val onClickAction: (Item) -> Unit) : HomeItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
