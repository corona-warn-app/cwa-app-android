package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.databinding.PersonOverviewItemBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PersonCertificateCard(parent: ViewGroup) :
    PersonOverviewAdapter.PersonOverviewItemVH<PersonCertificateCard.Item, PersonOverviewItemBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        PersonOverviewItemBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: PersonOverviewItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        name.text = curItem.certificate.fullName

        qrCodeLoadingIndicator.isInvisible = curItem.qrcodeBitmap != null
        qrcodeImage.apply {
            setImageBitmap(curItem.qrcodeBitmap)
            isInvisible = curItem.qrcodeBitmap == null
        }

        backgroundImage.setImageResource(curItem.colorShade.background)
        starsImage.setImageDrawable(starsDrawable(curItem))

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, adapterPosition) }
            transitionName = curItem.certificate.personIdentifier.codeSHA256
        }
        when (curItem.certificate.getState()) {
            is CwaCovidCertificate.State.ExpiringSoon -> {
                expirationStatusIcon.visibility = View.VISIBLE
                expirationStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_av_timer))
                expirationStatusText.visibility = View.VISIBLE
                expirationStatusText.text = context.getString(
                    R.string.certificate_qr_expiration,
                    curItem.certificate.headerExpiresAt.toShortDayFormat(),
                    curItem.certificate.headerExpiresAt.toShortTimeFormat()
                )
            }

            is CwaCovidCertificate.State.Expired -> {
                expirationStatusIcon.visibility = View.VISIBLE
                expirationStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error_outline))
                expirationStatusText.visibility = View.VISIBLE
                expirationStatusText.text = context.getText(R.string.certificate_qr_expired)
            }

            is CwaCovidCertificate.State.Valid -> {
                expirationStatusIcon.visibility = View.VISIBLE
                expirationStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error_outline))
                expirationStatusText.visibility = View.VISIBLE
                expirationStatusText.text = context.getText(R.string.certificate_qr_invalid_signature)
            }

            else -> {
            }
        }
    }

    private fun starsDrawable(item: Item) =
        context.getDrawableCompat(R.drawable.ic_eu_stars_blue)?.let {
            DrawableCompat.wrap(it)
                .mutate()
                .apply {
                    setTint(context.getColorCompat(item.colorShade.starsTint))
                }
        }

    data class Item(
        val certificate: CwaCovidCertificate,
        val qrcodeBitmap: Bitmap?,
        val colorShade: PersonColorShade,
        val onClickAction: (Item, Int) -> Unit,
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
