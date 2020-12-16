package de.rki.coronawarnapp.ui.main.home.items.testresult

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardUnregisteredBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestUnregisteredCard.Item

class TestUnregisteredCard(parent: ViewGroup) :
    HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardUnregisteredBinding>(
        R.layout.home_card_container_layout, parent
    ) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardUnregisteredBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionStatusCardUnregisteredBinding.(item: Item) -> Unit = { item ->
        itemView.setOnClickListener { item.onClickAction(item) }
        nextStepsAction.setOnClickListener { item.onClickAction(item) }
    }

    data class Item(
        val state: NoTest,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem
}
