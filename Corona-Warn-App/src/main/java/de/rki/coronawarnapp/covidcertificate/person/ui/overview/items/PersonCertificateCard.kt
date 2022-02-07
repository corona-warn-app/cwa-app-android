package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.core.VerificationCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.databinding.PersonOverviewItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.setUIState

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
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item

        val firstCertificate = curItem.verificationCertificates[0]
        val secondCertificate = curItem.verificationCertificates.getOrNull(1)
        setUIState(
            primaryCertificate = firstCertificate.cwaCertificate,
            primaryCertificateButtonText = firstCertificate.buttonText,
            secondaryCertificate = secondCertificate?.cwaCertificate,
            secondaryCertificateButtonText = secondCertificate?.buttonText,
            colorShade = curItem.colorShade,
            statusBadgeText = curItem.admissionBadgeText,
            badgeCount = curItem.badgeCount,
            onCovPassInfoAction = curItem.onCovPassInfoAction
        )

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, bindingAdapterPosition) }
            transitionName = firstCertificate.cwaCertificate.personIdentifier.codeSHA256
        }
    }

    data class Item(
        val verificationCertificates: List<VerificationCertificate>,
        val primaryCertificateText: String = "",
        val secondaryCertificateText: String = "",
        val admissionBadgeText: String = "",
        val colorShade: PersonColorShade,
        val badgeCount: Int,
        val onClickAction: (Item, Int) -> Unit,
        val onCovPassInfoAction: () -> Unit
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override val stableId: Long =
            verificationCertificates[0].cwaCertificate.personIdentifier.hashCode().toLong()
    }
}
