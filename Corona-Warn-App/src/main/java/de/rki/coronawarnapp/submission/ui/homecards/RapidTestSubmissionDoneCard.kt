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
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
        val userDate = curItem.state.getFormattedRegistrationDate()
        date.text = resources.getString(R.string.ag_homescreen_card_rapid_body_result_date, userDate)
    }

    data class Item(
        val state: SubmissionStateRAT.SubmissionDone,
        val onClickAction: (Item) -> Unit,
    ) : TestResultItem.RA
}
