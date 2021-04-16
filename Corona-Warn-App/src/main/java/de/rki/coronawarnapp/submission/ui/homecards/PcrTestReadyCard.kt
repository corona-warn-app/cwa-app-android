package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.databinding.HomeSubmissionPcrStatusCardReadyBinding
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestReadyCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PcrTestReadyCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionPcrStatusCardReadyBinding>(R.layout.home_card_container_layout, parent) {

    override val viewBinding = lazy {
        HomeSubmissionPcrStatusCardReadyBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: HomeSubmissionPcrStatusCardReadyBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
        showTestAction.setOnClickListener { itemView.performClick() }
    }

    data class Item(
        val state: SubmissionStatePCR.TestResultReady,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem.PCR, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
