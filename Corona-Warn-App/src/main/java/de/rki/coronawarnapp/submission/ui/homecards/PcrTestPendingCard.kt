package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.databinding.HomeSubmissionPcrStatusCardPendingBinding
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPendingCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PcrTestPendingCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionPcrStatusCardPendingBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionPcrStatusCardPendingBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionPcrStatusCardPendingBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
        showTestAction.setOnClickListener { itemView.performClick() }
    }

    data class Item(
        val state: SubmissionStatePCR.TestPending,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem.PCR, HasPayloadDiffer
}
