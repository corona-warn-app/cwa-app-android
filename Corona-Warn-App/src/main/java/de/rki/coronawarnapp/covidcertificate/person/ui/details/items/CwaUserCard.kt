package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.CwaUserCardItemBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

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
                formatBirthDate(certificate.dateOfBirthFormatted)
            )
            descriptionText.text = context.getString(
                R.string.person_details_cwa_user_description,
                certificate.fullName
            )
            cwaUserSwitch.setOnCheckedChangeListener(null)
            cwaUserSwitch.isChecked = curItem.personCertificates.isCwaUser
            cwaUserSwitch.setOnCheckedChangeListener { _, isChecked -> onSwitch(isChecked) }
        }
    }

    private fun formatBirthDate(dateOfBirthFormatted: String): String =
        try {
            LocalDate.parse(dateOfBirthFormatted, format).toDayFormat()
        } catch (e: Exception) {
            Timber.d(e, "Formatting to dd.MM.yyyy failed, falling back to $dateOfBirthFormatted")
            dateOfBirthFormatted
        }

    data class Item(
        val personCertificates: PersonCertificates,
        val onSwitch: (Boolean) -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = Item::class.hashCode().toLong()
    }

    companion object {
        private val format = DateTimeFormat.forPattern("yyyy-MM-dd")
    }
}
