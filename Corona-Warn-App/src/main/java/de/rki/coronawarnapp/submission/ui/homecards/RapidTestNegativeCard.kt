package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.HomeSubmissionRapidStatusCardNegativeBinding
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestNegativeCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RapidTestNegativeCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionRapidStatusCardNegativeBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionRapidStatusCardNegativeBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionRapidStatusCardNegativeBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        val userDate = curItem.state.getFormattedRegistrationDate()
        date.text = resources.getString(R.string.ag_homescreen_card_rapid_body_result_date, userDate)

        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        val state: SubmissionStateRAT.TestNegative,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem.RA, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
