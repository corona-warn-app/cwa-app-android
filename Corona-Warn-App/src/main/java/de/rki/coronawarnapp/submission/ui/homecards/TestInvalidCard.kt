package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardInvalidBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalidCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TestInvalidCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardInvalidBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardInvalidBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionStatusCardInvalidBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        deleteTestAction.setOnClickListener { item.onDeleteTest(item) }
    }

    data class Item(
        val state: TestInvalid,
        val onDeleteTest: (Item) -> Unit
    ) : TestResultItem
}
