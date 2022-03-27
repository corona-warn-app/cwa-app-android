package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.rapidantigen.SubmissionStateRAT
import de.rki.coronawarnapp.databinding.FamilyRapidTestCardPositivBinding
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestPositiveCard.Item
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyRapidTestPositiveCard(
    parent: ViewGroup
) : FamilyTestListAdapter.FamilyTestListVH<Item, FamilyRapidTestCardPositivBinding> (
    R.layout.family_rapid_test_card_positiv,
    parent
) {

    override val viewBinding = lazy {
        FamilyRapidTestCardPositivBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyRapidTestCardPositivBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        val state: SubmissionStateRAT.TestPositive,
        val onClickAction: (Item) -> Unit
    ) : FamilyTestListItem.RA, HasPayloadDiffer
}
