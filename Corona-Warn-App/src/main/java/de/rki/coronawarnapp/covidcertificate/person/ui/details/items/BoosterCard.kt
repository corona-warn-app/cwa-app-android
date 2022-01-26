package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.annotation.SuppressLint
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.PersonDetailsBoosterCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class BoosterCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<BoosterCard.Item, PersonDetailsBoosterCardBinding>(
        layoutRes = R.layout.person_details_booster_card,
        parent = parent
    ) {
    override val viewBinding: Lazy<PersonDetailsBoosterCardBinding> = lazy {
        PersonDetailsBoosterCardBinding.bind(itemView)
    }

    @SuppressLint("SetTextI18n")
    override val onBindData: PersonDetailsBoosterCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        root.setOnClickListener { curItem.onClick() }

        // TODO: replace with correct booster data
        title.text = "Booster"
        subtitle.text = "Empfehlung einer Booster Impfung"
//        val boosterNotification = curItem.boosterNotificationData
//        title.text = boosterNotification.titleText
//        subtitle.text = boosterNotification.subtitleText
    }

    data class Item(
        // TODO: replace with correct booster data
//        val boosterNotificationData: BoosterNotificationData,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {

        override val stableId = Item::class.hashCode().toLong()
    }
}
