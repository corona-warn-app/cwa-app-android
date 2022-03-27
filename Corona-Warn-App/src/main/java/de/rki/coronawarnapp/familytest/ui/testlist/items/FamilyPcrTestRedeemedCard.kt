package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.databinding.FamilyPcrTestCardRedeemedBinding
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestRedeemedCard.Item
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyPcrTestRedeemedCard(
    parent: ViewGroup
) : FamilyTestListAdapter.FamilyTestListVH<Item, FamilyPcrTestCardRedeemedBinding> (
    R.layout.family_pcr_test_card_redeemed,
    parent
) {

    override val viewBinding = lazy {
        FamilyPcrTestCardRedeemedBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyPcrTestCardRedeemedBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
        deleteTestAction.setOnClickListener { curItem.onDeleteTest(item) }
    }

    data class Item(
        val state: SubmissionStatePCR.TestError, // TODO: redeemed status?
        val onClickAction: (Item) -> Unit, // TODO: do we need it?
        val onDeleteTest: (Item) -> Unit
    ) : FamilyTestListItem.PCR, HasPayloadDiffer
}
