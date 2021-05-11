package de.rki.coronawarnapp.vaccination.ui.homecard

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationHomeRegistrationCardBinding
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class CreateVaccinationHomeCard(parent: ViewGroup) :
    HomeAdapter.HomeItemVH<CreateVaccinationHomeCard.Item, VaccinationHomeRegistrationCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        VaccinationHomeRegistrationCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: VaccinationHomeRegistrationCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        fun onClick() {
            val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
            curItem.onClickAction(item)
        }

        itemView.setOnClickListener {
            onClick()
        }
        nextStepsAction.setOnClickListener {
            onClick()
        }
    }

    data class Item(val onClickAction: (Item) -> Unit) : HomeItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
