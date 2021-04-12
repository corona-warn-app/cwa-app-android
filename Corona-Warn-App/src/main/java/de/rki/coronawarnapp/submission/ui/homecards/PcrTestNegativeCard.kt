package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionPcrStatusCardNegativeBinding
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestNegativeCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PcrTestNegativeCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionPcrStatusCardNegativeBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionPcrStatusCardNegativeBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionPcrStatusCardNegativeBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
        // TODO: we dont have show test button anymore. After confirmation should be removed
        // showTestAction.setOnClickListener { itemView.performClick() }
    }

    data class Item(
        val state: TestNegative,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
