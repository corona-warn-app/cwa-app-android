package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.HomeSubmissionRapidStatusCardPositiveNotSharedBinding
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestPositiveCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RapidTestPositiveCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionRapidStatusCardPositiveNotSharedBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionRapidStatusCardPositiveNotSharedBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionRapidStatusCardPositiveNotSharedBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        val userDate = curItem.state.getFormattedRegistrationDate()
        date.text = resources.getString(R.string.ag_homescreen_card_rapid_body_result_date, userDate)

        itemView.setOnClickListener { curItem.onClickAction(item) }
        submissionStatusCardPositiveButton.setOnClickListener { itemView.performClick() }
    }

    data class Item(
        val state: SubmissionStateRAT.TestPositive,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem.RA, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
