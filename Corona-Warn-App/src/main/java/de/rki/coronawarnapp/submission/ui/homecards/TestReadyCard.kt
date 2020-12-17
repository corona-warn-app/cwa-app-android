package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardReadyBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestReadyCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TestReadyCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardReadyBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardReadyBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionStatusCardReadyBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        itemView.setOnClickListener { item.onClickAction(item) }
        showTestAction.setOnClickListener { item.onClickAction(item) }
    }

    data class Item(
        val state: TestResultReady,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem
}
