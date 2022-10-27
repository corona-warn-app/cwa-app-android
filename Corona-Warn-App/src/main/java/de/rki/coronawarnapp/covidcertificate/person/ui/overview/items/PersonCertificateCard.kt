package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.AdmissionState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.MaskState
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
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

        val firstCertificate = curItem.overviewCertificates[0]
        setUIState(curItem)

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, bindingAdapterPosition) }
            transitionName = firstCertificate.cwaCertificate.personIdentifier.groupingKey
        }
    }

    data class Item(
        val overviewCertificates: List<OverviewCertificate>,
        val primaryCertificateText: String = "",
        val secondaryCertificateText: String = "",
        val admission: Admission,
        val mask: Mask,
        val certificateSelection: CertificateSelection = CertificateSelection.FIRST,
        val colorShade: PersonColorShade,
        val badgeCount: Int,
        val onClickAction: (Item, Int) -> Unit,
        val onCovPassInfoAction: () -> Unit,
        val onCertificateSelected: (certificateSelection: CertificateSelection) -> Unit,
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override val stableId: Long =
            overviewCertificates[0].cwaCertificate.personIdentifier.hashCode().toLong()

        data class OverviewCertificate(
            val cwaCertificate: CwaCovidCertificate,
            val buttonText: String = ""
        )

        enum class CertificateSelection {
            FIRST,
            SECOND,
            THIRD
        }

        data class Admission(
            val state: AdmissionState?,
            val text: String
        )

        data class Mask(
            val state: MaskState?,
            val text: String
        )
    }
}
