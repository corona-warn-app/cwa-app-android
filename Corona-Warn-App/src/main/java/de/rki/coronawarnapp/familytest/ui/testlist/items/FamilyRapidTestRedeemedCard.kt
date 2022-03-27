package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.FamilyRapidTestCardRedeemedBinding
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestRedeemedCard.Item
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyRapidTestRedeemedCard(
    parent: ViewGroup
) : FamilyTestListAdapter.FamilyTestListVH<Item, FamilyRapidTestCardRedeemedBinding> (
    R.layout.family_rapid_test_card_redeemed,
    parent
) {

    override val viewBinding = lazy {
        FamilyRapidTestCardRedeemedBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyRapidTestCardRedeemedBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
        deleteTestAction.setOnClickListener { curItem.onDeleteTest(item) }
    }

    data class Item(
        val state: SubmissionStateRAT.TestError, // TODO: redeemed status?
        val onClickAction: (Item) -> Unit, // TODO: do we need it?
        val onDeleteTest: (Item) -> Unit
    ) : FamilyTestListItem.RA, HasPayloadDiffer
}
