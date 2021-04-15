package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.HomeSubmissionRapidStatusCardPositiveSharedBinding
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestSubmissionDoneCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class RapidTestSubmissionDoneCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionRapidStatusCardPositiveSharedBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionRapidStatusCardPositiveSharedBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionRapidStatusCardPositiveSharedBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ -> }

    data class Item(
        val state: SubmissionStateRAT.SubmissionDone
    ) : TestResultItem
}
