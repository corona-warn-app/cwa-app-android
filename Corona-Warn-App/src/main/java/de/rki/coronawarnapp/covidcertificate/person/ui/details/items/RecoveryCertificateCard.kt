package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.RecoveryCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RecoveryCertificateCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<RecoveryCertificateCard.Item, RecoveryCertificateCardBinding>(
        layoutRes = R.layout.recovery_certificate_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<RecoveryCertificateCardBinding> = lazy {
        RecoveryCertificateCardBinding.bind(itemView)
    }
    override val onBindData: RecoveryCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        val certificate = curItem.certificate
        root.setOnClickListener { curItem.onClick() }

        certificateDate.text = context.getString(
            R.string.recovery_certificate_valid_until,
            certificate.validUntil?.toShortDayFormat() ?: certificate.rawCertificate.recovery.du
        )

        val bookmarkIcon =
            if (curItem.certificate.isDisplayValid) curItem.colorShade.bookmarkIcon else R.drawable.ic_bookmark
        currentCertificate.isVisible = curItem.isCurrentCertificate
        bookmark.setImageResource(bookmarkIcon)

        val color = when {
            curItem.certificate.isDisplayValid -> curItem.colorShade
            else -> PersonColorShade.COLOR_INVALID
        }

        when {
            curItem.certificate.isDisplayValid -> R.drawable.ic_recovery_certificate
            else -> R.drawable.ic_certificate_invalid
        }.also { certificateIcon.setImageResource(it) }

        when {
            curItem.isCurrentCertificate -> color.currentCertificateBg
            else -> color.defaultCertificateBg
        }.also { certificateBg.setImageResource(it) }

        notificationBadge.isVisible = curItem.certificate.hasNotificationBadge

        certificateExpiration.displayExpirationState(curItem.certificate)
    }

    data class Item(
        val certificate: RecoveryCertificate,
        val isCurrentCertificate: Boolean,
        val colorShade: PersonColorShade,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override val stableId: Long = certificate.containerId.hashCode().toLong()
    }
}
