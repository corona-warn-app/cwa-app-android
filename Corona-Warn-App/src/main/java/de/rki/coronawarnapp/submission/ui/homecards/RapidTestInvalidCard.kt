package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.HomeSubmissionRapidStatusCardInvalidBinding
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestInvalidCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RapidTestInvalidCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionRapidStatusCardInvalidBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionRapidStatusCardInvalidBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionRapidStatusCardInvalidBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        deleteTestAction.setOnClickListener { curItem.onDeleteTest(item) }
    }

    data class Item(
        val state: SubmissionStateRAT.TestInvalid,
        val onDeleteTest: (Item) -> Unit
    ) : TestResultItem.RA, HasPayloadDiffer
}
