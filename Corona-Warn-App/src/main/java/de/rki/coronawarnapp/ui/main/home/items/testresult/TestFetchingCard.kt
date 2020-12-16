package de.rki.coronawarnapp.ui.main.home.items.testresult

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardFetchingBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestFetchingCard.Item

class TestFetchingCard(parent: ViewGroup) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardFetchingBinding>(
    R.layout.home_card_container_layout, parent
) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardFetchingBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionStatusCardFetchingBinding.(item: Item) -> Unit = { item ->

    }

    data class Item(
        val state: FetchingResult
    ) : TestResultItem
}
