package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.RecoveryCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
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

        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val certificate = curItem.certificate
        root.setOnClickListener { curItem.onClick() }

        certificateDate.text = context.getString(
            R.string.recovery_certificate_valid_until,
            certificate.validUntil.toShortDayFormat()
        )

        currentCertificate.isVisible = curItem.isCurrentCertificate

        val background = when {
            curItem.isCurrentCertificate -> curItem.colorShade.currentCertificateBg
            else -> curItem.colorShade.defaultCertificateBg
        }
        certificateBg.setImageResource(background)

        when (certificate.getState()) {
            is CwaCovidCertificate.State.ExpiringSoon -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getString(
                    R.string.certificate_person_details_card_expiration,
                    curItem.certificate.headerExpiresAt.toShortDayFormat(),
                    curItem.certificate.headerExpiresAt.toShortTimeFormat()
                )
            }

            is CwaCovidCertificate.State.Expired -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getText(R.string.certificate_qr_expired)
            }

            is CwaCovidCertificate.State.Invalid -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getText(R.string.certificate_qr_invalid_signature)
            }

            else -> {
            }
        }
    }

    data class Item(
        val certificate: RecoveryCertificate,
        val isCurrentCertificate: Boolean,
        val colorShade: PersonColorShade,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate.containerId.hashCode().toLong()
    }
}
