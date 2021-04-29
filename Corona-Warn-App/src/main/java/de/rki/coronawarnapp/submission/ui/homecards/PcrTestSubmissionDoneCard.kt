package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.databinding.HomeSubmissionPcrStatusCardPositiveSharedBinding
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestSubmissionDoneCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class PcrTestSubmissionDoneCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionPcrStatusCardPositiveSharedBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionPcrStatusCardPositiveSharedBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionPcrStatusCardPositiveSharedBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
        val userDate = curItem.state.getFormattedRegistrationDate()
        date.text = resources.getString(R.string.ag_homescreen_card_pcr_body_result_date, userDate)
    }

    data class Item(
        val state: SubmissionStatePCR.SubmissionDone,
        val onClickAction: (Item) -> Unit,
    ) : TestResultItem.PCR
}
