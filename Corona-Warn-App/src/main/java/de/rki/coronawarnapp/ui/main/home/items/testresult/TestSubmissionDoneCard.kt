package de.rki.coronawarnapp.ui.main.home.items.testresult

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardDoneBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestSubmissionDoneCard.Item

class TestSubmissionDoneCard(parent: ViewGroup) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardDoneBinding>(
    R.layout.home_card_container_layout, parent
) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardDoneBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionStatusCardDoneBinding.(item: Item) -> Unit = { item ->

    }

    data class Item(
        val state: SubmissionDone
    ) : TestResultItem
}
