package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.SubmissionStatePCR
import de.rki.coronawarnapp.databinding.FamilyPcrTestCardInvalidBinding
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestInvalidCard.Item
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyPcrTestInvalidCard(
    parent: ViewGroup
) : FamilyTestListAdapter.FamilyTestListVH<Item, FamilyPcrTestCardInvalidBinding> (
    R.layout.family_pcr_test_card_invalid,
    parent
) {

    override val viewBinding = lazy {
        FamilyPcrTestCardInvalidBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyPcrTestCardInvalidBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        val state: SubmissionStatePCR.TestInvalid,
        val onClickAction: (Item) -> Unit
    ) : FamilyTestListItem.PCR, HasPayloadDiffer
}
