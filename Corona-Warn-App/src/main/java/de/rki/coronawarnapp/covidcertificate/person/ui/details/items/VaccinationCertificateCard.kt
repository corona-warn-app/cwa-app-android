package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard.Item
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.BOOSTER_ELIGIBLE
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.IMMUNITY
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.VaccinationCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class VaccinationCertificateCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<Item, VaccinationCertificateCardBinding>(
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

        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        val certificate = curItem.certificate
        root.setOnClickListener { curItem.onClick() }
        vaccinationDosesInfo.text = context.getString(
            R.string.vaccination_certificate_doses,
            certificate.doseNumber,
            certificate.totalSeriesOfDoses
        )

        certificateDate.text = context.getString(
            R.string.vaccination_certificate_vaccinated_on,
            certificate.vaccinatedOn.toShortDayFormat()
        )
        val bookmarkIcon =
            if (curItem.certificate.isDisplayValid) R.drawable.ic_bookmark_blue else R.drawable.ic_bookmark
        currentCertificate.isVisible = curItem.isCurrentCertificate
        bookmark.setImageResource(bookmarkIcon)

        val color = when {
            curItem.certificate.isDisplayValid -> curItem.colorShade
            else -> PersonColorShade.COLOR_INVALID
        }

        when {
            // Invalid state first
            !certificate.isDisplayValid -> R.drawable.ic_certificate_invalid

            // Final shot
            certificate.isSeriesCompletingShot -> when (curItem.status) {
                IMMUNITY, BOOSTER_ELIGIBLE -> R.drawable.ic_vaccination_immune
                else -> R.drawable.ic_vaccination_complete
            }

            // Other shots
            else -> R.drawable.ic_vaccination_incomplete
        }.also { certificateIcon.setImageResource(it) }

        when {
            curItem.isCurrentCertificate -> color.currentCertificateBg
            else -> color.defaultCertificateBg
        }.also { certificateBg.setImageResource(it) }

        notificationBadge.isVisible = curItem.certificate.hasNotificationBadge

        certificateExpiration.displayExpirationState(curItem.certificate)
    }

    data class Item(
        val certificate: VaccinationCertificate,
        val colorShade: PersonColorShade,
        val isCurrentCertificate: Boolean,
        val status: VaccinatedPerson.Status,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
