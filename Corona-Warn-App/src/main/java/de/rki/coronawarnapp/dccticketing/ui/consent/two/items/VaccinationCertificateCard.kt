package de.rki.coronawarnapp.dccticketing.ui.consent.two.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.IMMUNITY
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.VaccinationCertificateCardBinding
import de.rki.coronawarnapp.dccticketing.ui.consent.two.DccConsentTwoAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class VaccinationCertificateCard(parent: ViewGroup) :
    DccConsentTwoAdapter.DccConsentTwoItemVH<VaccinationCertificateCard.Item, VaccinationCertificateCardBinding>(
        layoutRes = R.layout.vaccination_certificate_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationCertificateCardBinding> = lazy {
        VaccinationCertificateCardBinding.bind(itemView)
    }
    override val onBindData: VaccinationCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val certificate = curItem.certificate

        vaccinationDosesInfo.text = context.getString(
            R.string.vaccination_certificate_doses,
            certificate.doseNumber,
            certificate.totalSeriesOfDoses
        )
        certificateDate.text = context.getString(
            R.string.vaccination_certificate_vaccinated_on,
            certificate.vaccinatedOn.toShortDayFormat()
        )

        when {
            // Invalid state first
            !certificate.isValid -> R.drawable.ic_certificate_invalid

            // Final shot
            certificate.isSeriesCompletingShot -> when (curItem.status) {
                IMMUNITY -> R.drawable.ic_vaccination_immune
                else -> R.drawable.ic_vaccination_complete
            }

            // Other shots
            else -> R.drawable.ic_vaccination_incomplete
        }.also { certificateIcon.setImageResource(it) }

        certificateBg.setImageResource(PersonColorShade.COLOR_1.currentCertificateBg)

        notificationBadge.isVisible = false
        arrow.isVisible = false
        bookmark.isVisible = false

        certificateExpiration.displayExpirationState(curItem.certificate)
    }

    data class Item(
        val certificate: VaccinationCertificate,
        val status: VaccinatedPerson.Status,
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
