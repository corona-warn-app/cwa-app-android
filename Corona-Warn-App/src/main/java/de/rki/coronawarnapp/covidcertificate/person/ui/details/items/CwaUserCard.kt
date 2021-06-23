package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.CwaUserCardItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class CwaUserCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<CwaUserCard.Item, CwaUserCardItemBinding>(
        layoutRes = R.layout.cwa_user_card_item,
        parent = parent
    ) {
    override val viewBinding: Lazy<CwaUserCardItemBinding> = lazy {
        CwaUserCardItemBinding.bind(itemView)
    }

    override val onBindData: CwaUserCardItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val certificate = curItem.personCertificates.highestPriorityCertificate
        curItem.apply {
            userName.text = certificate.fullName
            dateOfBirth.text = context.getString(
                R.string.person_details_cwa_user_birthdate,
                certificate.dateOfBirthFormatted
            )
            cwaUserSwitch.isChecked = curItem.personCertificates.isCwaUser
            cwaUserSwitch.setOnCheckedChangeListener { _, isChecked -> onSwitch(isChecked) }
        }
    }

    data class Item(
        val personCertificates: PersonCertificates,
        val onSwitch: (Boolean) -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = Item::class.hashCode().toLong()
    }
}
