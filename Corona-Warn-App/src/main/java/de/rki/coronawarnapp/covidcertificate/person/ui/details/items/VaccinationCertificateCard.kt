package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard.Item
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.VaccinationCertificateCardBinding
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class VaccinationCertificateCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<Item, VaccinationCertificateCardBinding>(
        layoutRes = R.layout.vaccination_certificate_card,
        parent = parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.certificate, holder.bindingAdapterPosition) }
    }

    override val viewBinding: Lazy<VaccinationCertificateCardBinding> = lazy {
        VaccinationCertificateCardBinding.bind(itemView)
    }
    override val onBindData: VaccinationCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { boundItem, payloads ->

        latestItem = payloads.filterIsInstance<Item>().lastOrNull() ?: boundItem

        latestItem?.let { item ->
            val certificate = item.certificate
            root.setOnClickListener { item.onClick() }
            vaccinationDosesInfo.text = context.getString(
                R.string.vaccination_certificate_doses,
                certificate.doseNumber,
                certificate.totalSeriesOfDoses
            )

            certificateDate.text = context.getString(
                R.string.vaccination_certificate_vaccinated_on,
                certificate.vaccinatedOn?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    ?: certificate.rawCertificate.vaccination.dt
            )
            val bookmarkIcon = if (item.certificate.isDisplayValid)
                item.colorShade.bookmarkIcon else R.drawable.ic_bookmark
            currentCertificateGroup.isVisible = item.isCurrentCertificate
            bookmark.setImageResource(bookmarkIcon)

            val color = when {
                item.certificate.isDisplayValid -> item.colorShade
                else -> PersonColorShade.COLOR_INVALID
            }

            when {
                // Invalid state first
                !certificate.isDisplayValid -> R.drawable.ic_certificate_invalid
                // Final shot
                certificate.isSeriesCompletingShot -> VaccinationCertificate.iconComplete
                // Other shots
                else -> VaccinationCertificate.iconIncomplete
            }.also { certificateIcon.setImageResource(it) }

            when {
                item.isCurrentCertificate -> color.currentCertificateBg
                else -> color.defaultCertificateBg
            }.also { certificateBg.setImageResource(it) }

            notificationBadge.isVisible = item.certificate.hasNotificationBadge
            certificateExpiration.displayExpirationState(item.certificate)

            startValidationCheckButton.apply {
                isActive = certificate.isNotScreened
                isLoading = item.isLoading
                setOnClickListener {
                    item.validateCertificate(certificate.containerId)
                }
            }
        }
    }

    data class Item(
        val certificate: VaccinationCertificate,
        val colorShade: PersonColorShade,
        val isCurrentCertificate: Boolean,
        val isLoading: Boolean = false,
        val isAppEol: Boolean = false,
        val onClick: () -> Unit,
        val onSwipeItem: (VaccinationCertificate, Int) -> Unit,
        val validateCertificate: (CertificateContainerId) -> Unit,
    ) : CertificateItem, HasPayloadDiffer {
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
