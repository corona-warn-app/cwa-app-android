package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import coil.loadAny
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.getValidQrCode
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.databinding.PersonOverviewItemBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.bindValidityViews
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.mutateDrawable
import java.util.Locale

class PersonCertificateCard(parent: ViewGroup) :
    PersonOverviewAdapter.PersonOverviewItemVH<PersonCertificateCard.Item, PersonOverviewItemBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        PersonOverviewItemBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    // TODO: Update curItem with the right certificate
    override val onBindData: PersonOverviewItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        val color = when {
            curItem.certificate2G!!.isValid -> curItem.colorShade
            else -> PersonColorShade.COLOR_INVALID
        }
        name.text = curItem.certificate2G.fullName

        qrCodeCard.image.loadAny(curItem.certificate2G.getValidQrCode(Locale.getDefault().language)) {
            crossfade(true)
            loadingView(qrCodeCard.image, qrCodeCard.progressBar)
        }

        backgroundImage.setImageResource(color.background)
        starsImage.setImageDrawable(starsDrawable(color))

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, adapterPosition) }
            transitionName = curItem.certificate2G.personIdentifier.codeSHA256
        }
        curItem.certificate2G.let {
            qrCodeCard.bindValidityViews(
                it,
                isPersonOverview = true,
                badgeCount = curItem.badgeCount,
                onCovPassInfoAction = curItem.onCovPassInfoAction
            )
        }
    }

    private fun starsDrawable(colorShade: PersonColorShade) =
        resources.mutateDrawable(
            R.drawable.ic_eu_stars_blue,
            context.getColorCompat(colorShade.starsTint)
        )

    // TODO: Remove default value for admissionState once logic is implemented
    data class Item(
        val certificate2G: CwaCovidCertificate? = null,
        val certificateTest: CwaCovidCertificate? = null,
        val admissionState: PersonCertificates.AdmissionState = PersonCertificates.AdmissionState.OTHER,
        val colorShade: PersonColorShade,
        val badgeCount: Int,
        val onClickAction: (Item, Int) -> Unit,
        val onCovPassInfoAction: () -> Unit
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate2G?.personIdentifier?.codeSHA256?.hashCode()?.toLong()
            ?: certificateTest!!.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
