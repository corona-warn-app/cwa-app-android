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

        setUIState(
            primaryCertificate = curItem.certificatesForOverviewScreen[0].certificate,
            secondaryCertificate = curItem.certificatesForOverviewScreen.getOrNull(1)?.certificate,
            primaryCertificateButtonText = curItem.certificatesForOverviewScreen[0].buttonText,
            secondaryCertificateButtonText = curItem.certificatesForOverviewScreen.getOrNull(1)?.buttonText,
            colorShade = curItem.colorShade,
            statusBadgeText = curItem.admissionBadgeText,
            badgeCount = curItem.badgeCount,
            onCovPassInfoAction = curItem.onCovPassInfoAction
        )

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, bindingAdapterPosition) }
            transitionName = curItem.certificatesForOverviewScreen[0].certificate.personIdentifier.codeSHA256
        }
    }

    data class Item(
        val certificatesForOverviewScreen: List<VerificationCertificate>,
        val primaryCertificateText: String = "",
        val secondaryCertificateText: String = "",
        val admissionBadgeText: String = "",
        val colorShade: PersonColorShade,
        val badgeCount: Int,
        val onClickAction: (Item, Int) -> Unit,
        val onCovPassInfoAction: () -> Unit
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override val stableId: Long = hashCode().toLong()
    }
}
