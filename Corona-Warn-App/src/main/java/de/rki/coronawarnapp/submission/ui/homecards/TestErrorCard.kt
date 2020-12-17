package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardErrorBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestErrorCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TestErrorCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardErrorBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardErrorBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionStatusCardErrorBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        itemView.setOnClickListener { item.onDeleteTest(item) }
        showTestAction.setOnClickListener { itemView.performClick() }
    }

    data class Item(
        val state: TestError,
        val onDeleteTest: (Item) -> Unit
    ) : TestResultItem
}
