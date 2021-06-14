package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.databinding.HomeSubmissionPcrStatusCardPositiveNotSharedBinding
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPositiveCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PcrTestPositiveCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionPcrStatusCardPositiveNotSharedBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionPcrStatusCardPositiveNotSharedBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionPcrStatusCardPositiveNotSharedBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        val userDate = curItem.state.getFormattedRegistrationDate()
        date.text = resources.getString(R.string.ag_homescreen_card_pcr_body_result_date, userDate)

        itemView.setOnClickListener { curItem.onClickAction(item) }
        submissionStatusCardPositiveButton.setOnClickListener { itemView.performClick() }
        submissionStatusCardPositiveButtonDelete.setOnClickListener { curItem.onRemoveAction() }
    }

    data class Item(
        val state: SubmissionStatePCR.TestPositive,
        val onClickAction: (Item) -> Unit,
        val onRemoveAction: () -> Unit
    ) : TestResultItem.PCR, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
