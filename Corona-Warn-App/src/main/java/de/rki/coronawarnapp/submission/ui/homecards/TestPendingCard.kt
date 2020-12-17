package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardPendingBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestPendingCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TestPendingCard(parent: ViewGroup) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardPendingBinding>(
    R.layout.home_card_container_layout, parent
) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardPendingBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionStatusCardPendingBinding.(item: Item) -> Unit = { item ->
        itemView.setOnClickListener { item.onClickAction(item) }
        showTestAction.setOnClickListener { item.onClickAction(item) }
    }

    data class Item(
        val state: TestPending,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem
}
