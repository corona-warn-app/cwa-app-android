package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.HomeSubmissionRapidStatusCardErrorBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RapidTestErrorCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<RapidTestErrorCard.Item, HomeSubmissionRapidStatusCardErrorBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionRapidStatusCardErrorBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionRapidStatusCardErrorBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        itemView.setOnClickListener { curItem.onDeleteTest(item) }
        showTestAction.setOnClickListener { itemView.performClick() }
    }

    data class Item(
        val state: SubmissionStateRAT.TestError,
        val onDeleteTest: (Item) -> Unit
    ) : TestResultItem.RA, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
