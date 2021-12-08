package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import coil.loadAny
import de.rki.coronawarnapp.R
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
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item

        val color = when {
            curItem.admissionState.primaryCertificate!!.isValid -> curItem.colorShade
            else -> PersonColorShade.COLOR_INVALID
        }
        name.text = curItem.admissionState.primaryCertificate.fullName

        qrCodeCard.image
            .loadAny(curItem.admissionState.primaryCertificate.getValidQrCode(Locale.getDefault().language)) {
                crossfade(true)
                loadingView(qrCodeCard.image, qrCodeCard.progressBar)
            }

        backgroundImage.setImageResource(color.background)
        starsImage.setImageDrawable(starsDrawable(color))

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, adapterPosition) }
            transitionName = curItem.admissionState.primaryCertificate.personIdentifier.codeSHA256
        }
        qrCodeCard.bindValidityViews(
            curItem.admissionState.primaryCertificate,
            isPersonOverview = true,
            badgeCount = curItem.badgeCount,
            onCovPassInfoAction = curItem.onCovPassInfoAction
        )
    }

    private fun starsDrawable(colorShade: PersonColorShade) =
        resources.mutateDrawable(
            R.drawable.ic_eu_stars_blue,
            context.getColorCompat(colorShade.starsTint)
        )

    data class Item(
        val admissionState: PersonCertificates.AdmissionState,
        val colorShade: PersonColorShade,
        val badgeCount: Int,
        val onClickAction: (Item, Int) -> Unit,
        val onCovPassInfoAction: () -> Unit
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override val stableId: Long =
            admissionState.primaryCertificate!!.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
