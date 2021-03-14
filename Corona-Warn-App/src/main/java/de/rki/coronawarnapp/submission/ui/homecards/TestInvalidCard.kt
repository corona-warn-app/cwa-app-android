package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeSubmissionStatusCardInvalidBinding
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalidCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class TestInvalidCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionStatusCardInvalidBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionStatusCardInvalidBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionStatusCardInvalidBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        deleteTestAction.setOnClickListener { curItem.onDeleteTest(item) }
    }

    data class Item(
        val state: TestInvalid,
        val onDeleteTest: (Item) -> Unit
    ) : TestResultItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
