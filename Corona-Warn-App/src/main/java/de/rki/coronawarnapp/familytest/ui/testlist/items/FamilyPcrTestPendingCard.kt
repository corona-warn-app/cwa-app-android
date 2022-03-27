package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.databinding.FamilyPcrTestCardPendingBinding
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestPendingCard.Item
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyPcrTestPendingCard(
    parent: ViewGroup
) : FamilyTestListAdapter.FamilyTestListVH<Item, FamilyPcrTestCardPendingBinding> (
    R.layout.family_pcr_test_card_pending,
    parent
) {

    override val viewBinding = lazy {
        FamilyPcrTestCardPendingBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyPcrTestCardPendingBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        val state: SubmissionStatePCR.TestPending,
        val onClickAction: (Item) -> Unit
    ) : FamilyTestListItem.PCR, HasPayloadDiffer
}
