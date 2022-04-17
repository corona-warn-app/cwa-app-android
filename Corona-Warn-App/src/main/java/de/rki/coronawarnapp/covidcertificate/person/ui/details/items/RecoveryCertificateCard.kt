package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.RecoveryCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RecoveryCertificateCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<RecoveryCertificateCard.Item, RecoveryCertificateCardBinding>(
        layoutRes = R.layout.recovery_certificate_card,
        parent = parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.certificate, holder.bindingAdapterPosition) }
    }

    override val viewBinding: Lazy<RecoveryCertificateCardBinding> = lazy {
        RecoveryCertificateCardBinding.bind(itemView)
    }
    override val onBindData: RecoveryCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        latestItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        val certificate = latestItem!!.certificate
        root.setOnClickListener { latestItem!!.onClick() }

        certificateDate.text = context.getString(
            R.string.recovery_certificate_sample_collection,
            certificate.testedPositiveOn?.toShortDayFormat() ?: certificate.rawCertificate.recovery.fr
        )

        val bookmarkIcon =
            if (latestItem!!.certificate.isDisplayValid) latestItem!!.colorShade.bookmarkIcon else R.drawable.ic_bookmark
        currentCertificateGroup.isVisible = latestItem!!.isCurrentCertificate
        bookmark.setImageResource(bookmarkIcon)

        val color = when {
            latestItem!!.certificate.isDisplayValid -> latestItem!!.colorShade
            else -> PersonColorShade.COLOR_INVALID
        }

        when {
            latestItem!!.certificate.isDisplayValid -> R.drawable.ic_recovery_certificate
            else -> R.drawable.ic_certificate_invalid
        }.also { certificateIcon.setImageResource(it) }

        when {
            latestItem!!.isCurrentCertificate -> color.currentCertificateBg
            else -> color.defaultCertificateBg
        }.also { certificateBg.setImageResource(it) }

        notificationBadge.isVisible = latestItem!!.certificate.hasNotificationBadge

        certificateExpiration.displayExpirationState(latestItem!!.certificate)

        startValidationCheckButton.apply {
            defaultButton.isEnabled = certificate.isNotBlocked
            isEnabled = certificate.isNotBlocked
            isLoading = latestItem!!.isLoading
            defaultButton.setOnClickListener {
                latestItem!!.validateCertificate(certificate.containerId)
            }
        }
    }

    data class Item(
        val certificate: RecoveryCertificate,
        val isCurrentCertificate: Boolean,
        val colorShade: PersonColorShade,
        val isLoading: Boolean = false,
        val onClick: () -> Unit,
        val onSwipeItem: (RecoveryCertificate, Int) -> Unit,
        val validateCertificate: (CertificateContainerId) -> Unit,
    ) : CertificateItem, HasPayloadDiffer {
        override val stableId: Long = certificate.containerId.hashCode().toLong()
    }
}
