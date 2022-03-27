package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.FamilyRapidTestCardPendingBinding
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestPendingCard.Item
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyRapidTestPendingCard(
    parent: ViewGroup
) : FamilyTestListAdapter.FamilyTestListVH<Item, FamilyRapidTestCardPendingBinding> (
    R.layout.family_rapid_test_card_pending,
    parent
) {

    override val viewBinding = lazy {
        FamilyRapidTestCardPendingBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyRapidTestCardPendingBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        val state: SubmissionStateRAT.TestPending,
        val onClickAction: (Item) -> Unit
    ) : FamilyTestListItem.RA, HasPayloadDiffer
}
