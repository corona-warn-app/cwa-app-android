package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import coil.loadAny
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.databinding.PersonOverviewItemBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.QrCodeHelper
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

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

        qrcodeImage.loadAny(
            CoilQrCode(content = curItem.certificate.qrCode)
        ) {
            crossfade(true)
            loadingView(qrcodeImage, qrCodeLoadingIndicator)
        }

        backgroundImage.setImageResource(curItem.colorShade.background)
        starsImage.setImageDrawable(starsDrawable(curItem))

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, adapterPosition) }
            transitionName = curItem.certificate.personIdentifier.codeSHA256
        }

        if (QrCodeHelper.isInvalidOrExpired(item.certificate.getState())) {
            qrcodeImage.alpha = 0.1f
            invalidQrCodeSymbol.isVisible = true
        } else {
            invalidQrCodeSymbol.isVisible = false
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
        val colorShade: PersonColorShade,
        val onClickAction: (Item, Int) -> Unit,
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
