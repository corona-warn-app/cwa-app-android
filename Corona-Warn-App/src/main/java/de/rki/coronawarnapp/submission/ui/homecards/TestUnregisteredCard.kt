package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardUnregisteredBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestUnregisteredCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TestUnregisteredCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardUnregisteredBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardUnregisteredBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionStatusCardUnregisteredBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        itemView.setOnClickListener { item.onClickAction(item) }
        nextStepsAction.setOnClickListener { item.onClickAction(item) }
    }

    data class Item(
        val state: NoTest,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem
}
