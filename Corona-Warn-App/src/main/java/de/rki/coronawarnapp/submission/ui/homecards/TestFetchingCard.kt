package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CommonSubmissionStates
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardFetchingBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TestFetchingCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardFetchingBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardFetchingBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionStatusCardFetchingBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ -> }

    data class Item(
        val state: CommonSubmissionStates.TestFetching
    ) : TestResultItem {
        override val stableId: Long
            get() = when (state) {
                is SubmissionStatePCR -> TestResultItem.PCR.LIST_ID
                is SubmissionStateRAT -> TestResultItem.RA.LIST_ID
                else -> throw IllegalArgumentException("Invalid card argument $state")
            }
    }
}
