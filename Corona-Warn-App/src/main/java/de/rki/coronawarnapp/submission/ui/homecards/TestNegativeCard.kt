package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardNegativeBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestNegativeCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TestNegativeCard(parent: ViewGroup) :
    HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardNegativeBinding>(
        R.layout.home_card_container_layout, parent
    ) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardNegativeBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionStatusCardNegativeBinding.(item: Item) -> Unit = { item ->
        itemView.setOnClickListener { item.onClickAction(item) }
        showTestAction.setOnClickListener { itemView.performClick() }
    }

    data class Item(
        val state: TestNegative,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem
}
