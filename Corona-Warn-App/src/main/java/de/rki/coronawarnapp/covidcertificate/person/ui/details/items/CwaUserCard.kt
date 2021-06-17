package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.CwaUserCardItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.LocalDate

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
        item.apply {
            userName.text = fullName
            birthDate.text = dateOfBirth.toString(DATE_FORMAT)
        }
    }

    companion object {
        private const val DATE_FORMAT = "dd.MM.yy"
    }

    data class Item(
        val certificate: CwaCovidCertificate,
        val fullName: String,
        val dateOfBirth: LocalDate
    ) : SpecificCertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = this.hashCode().toLong()
    }
}
