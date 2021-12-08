package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.databinding.HomeSubmissionPcrStatusCardErrorBinding
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestErrorCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PcrTestErrorCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionPcrStatusCardErrorBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionPcrStatusCardErrorBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionPcrStatusCardErrorBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onDeleteTest(item) }
        showTestAction.setOnClickListener { itemView.performClick() }
    }

    data class Item(
        val state: SubmissionStatePCR.TestError,
        val onDeleteTest: (Item) -> Unit
    ) : TestResultItem.PCR, HasPayloadDiffer
}
