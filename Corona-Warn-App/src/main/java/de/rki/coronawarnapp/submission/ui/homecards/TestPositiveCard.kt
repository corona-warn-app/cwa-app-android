package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardPositiveBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestPositiveCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TestPositiveCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardPositiveBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardPositiveBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionStatusCardPositiveBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        itemView.setOnClickListener { item.onClickAction(item) }
        submissionStatusCardPositiveButton.setOnClickListener { item.onClickAction(item) }
    }

    data class Item(
        val state: TestPositive,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem
}
