package de.rki.coronawarnapp.dccticketing.ui.consent.two.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.RecoveryCertificateCardBinding
import de.rki.coronawarnapp.dccticketing.ui.consent.two.DccConsentTwoAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.displayExpirationState
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RecoveryCertificateCard(parent: ViewGroup) :
    DccConsentTwoAdapter.DccConsentTwoItemVH<RecoveryCertificateCard.Item, RecoveryCertificateCardBinding>(
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

        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val certificate = curItem.certificate

        certificateDate.text = context.getString(
            R.string.recovery_certificate_valid_until,
            certificate.validUntil.toShortDayFormat()
        )

        certificateBg.setImageResource(PersonColorShade.COLOR_1.currentCertificateBg)

        notificationBadge.isVisible = false
        arrow.isVisible = false
        bookmark.isVisible = false

        certificateExpiration.displayExpirationState(curItem.certificate)
    }

    data class Item(
        val certificate: RecoveryCertificate,
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate.containerId.hashCode().toLong()
    }
}
