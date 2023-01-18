package de.rki.coronawarnapp.submission.ui.homecards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CommonSubmissionStates
import de.rki.coronawarnapp.databinding.HomeSubmissionRegisterTestCardBinding
import de.rki.coronawarnapp.submission.ui.homecards.RegisterTestCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RegisterTestCard(
    parent: ViewGroup
) : HomeAdapter.HomeItemVH<Item, HomeSubmissionRegisterTestCardBinding>(
    R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        HomeSubmissionRegisterTestCardBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeSubmissionRegisterTestCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item

        itemView.setOnClickListener { curItem.onClickAction(item) }
        registerTestCardContinue.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        val state: CommonSubmissionStates.TestUnregistered,
        val onClickAction: (Item) -> Unit
    ) : TestResultItem, HasPayloadDiffer
}
