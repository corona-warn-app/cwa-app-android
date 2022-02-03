package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.ui.text.format
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

        val boosterNotification = curItem.boosterNotification
        title.text = boosterNotification.titleText.format().orEmpty()
        subtitle.text = boosterNotification.subtitleText.format().orEmpty()

        boosterBadge.isVisible = curItem.isNew
    }

    data class Item(
        val boosterNotification: BoosterNotification,
        val isNew: Boolean = true,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {

        override val stableId = Item::class.hashCode().toLong()
    }
}
