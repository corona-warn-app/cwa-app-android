package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfoWrapper
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.Other
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.ThreeGWithPCR
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.ThreeGWithRAT
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoG
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlusPCR
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.TwoGPlusRAT
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
        val walletInfo = curItem.dccWalletInfoWrapper
        when (curItem.admissionState) {
            is TwoG -> {
                setUIState(
                    primaryCertificate = curItem.admissionState.twoGCertificate,
                    colorShade = curItem.colorShade,
                    statusBadgeText = walletInfo.admissionBadgeText.orEmpty(),
                    badgeCount = curItem.badgeCount,
                    onCovPassInfoAction = curItem.onCovPassInfoAction
                )
            }
            is TwoGPlusPCR -> {
                setUIState(
                    primaryCertificate = curItem.admissionState.twoGCertificate,
                    secondaryCertificate = curItem.admissionState.testCertificate,
                    colorShade = curItem.colorShade,
                    statusBadgeText = walletInfo.admissionBadgeText.orEmpty(),
                    badgeCount = curItem.badgeCount,
                    onCovPassInfoAction = curItem.onCovPassInfoAction
                )
            }

            is TwoGPlusRAT -> {
                setUIState(
                    primaryCertificate = curItem.admissionState.twoGCertificate,
                    secondaryCertificate = curItem.admissionState.testCertificate,
                    colorShade = curItem.colorShade,
                    statusBadgeText = walletInfo.admissionBadgeText.orEmpty(),
                    badgeCount = curItem.badgeCount,
                    onCovPassInfoAction = curItem.onCovPassInfoAction
                )
            }

            is ThreeGWithPCR -> {
                setUIState(
                    primaryCertificate = curItem.admissionState.testCertificate,
                    colorShade = curItem.colorShade,
                    statusBadgeText = walletInfo.admissionBadgeText.orEmpty(),
                    badgeCount = curItem.badgeCount,
                    onCovPassInfoAction = curItem.onCovPassInfoAction
                )
            }

            is ThreeGWithRAT -> {
                setUIState(
                    primaryCertificate = curItem.admissionState.testCertificate,
                    colorShade = curItem.colorShade,
                    statusBadgeText = walletInfo.admissionBadgeText.orEmpty(),
                    badgeCount = curItem.badgeCount,
                    onCovPassInfoAction = curItem.onCovPassInfoAction
                )
            }

            is Other -> {
                setUIState(
                    primaryCertificate = curItem.admissionState.otherCertificate,
                    colorShade = curItem.colorShade,
                    badgeCount = curItem.badgeCount,
                    onCovPassInfoAction = curItem.onCovPassInfoAction
                )
            }
        }

        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, bindingAdapterPosition) }
            transitionName = curItem.admissionState.primaryCertificate.personIdentifier.codeSHA256
        }
    }

    data class Item(
        val dccWalletInfoWrapper: DccWalletInfoWrapper = DccWalletInfoWrapper(),
        val admissionState: AdmissionState,
        val colorShade: PersonColorShade,
        val badgeCount: Int,
        val onClickAction: (Item, Int) -> Unit,
        val onCovPassInfoAction: () -> Unit
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override val stableId: Long =
            dccWalletInfoWrapper.hashCode().toLong()
    }
}
