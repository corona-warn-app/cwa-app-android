package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewItemColor
import de.rki.coronawarnapp.databinding.PersonOverviewItemBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
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
        qrcodeImage.setImageBitmap(curItem.qrcodeBitmap)
        backgroundImage.setImageResource(curItem.color.background)
        starsImage.setImageDrawable(starsDrawable(curItem))

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, adapterPosition) }
            transitionName = curItem.certificate.personIdentifier.codeSHA256
        }
    }

    private fun starsDrawable(item: Item) =
        context.getDrawableCompat(R.drawable.ic_eu_stars_blue)?.let {
            DrawableCompat.wrap(it)
                .mutate()
                .apply {
                    setTint(context.getColorCompat(item.color.starsTint))
                }
        }

    data class Item(
        val certificate: CwaCovidCertificate,
        val qrcodeBitmap: Bitmap?,
        val color: PersonOverviewItemColor,
        val onClickAction: (Item, Int) -> Unit,
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
