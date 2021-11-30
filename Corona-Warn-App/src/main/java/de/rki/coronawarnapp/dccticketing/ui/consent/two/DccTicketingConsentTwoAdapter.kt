package de.rki.coronawarnapp.dccticketing.ui.consent.two

import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateItem
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionAdapter
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingRecoveryCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingTestCard
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards.DccTicketingVaccinationCard
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class DccTicketingConsentTwoAdapter :
    ModularAdapter<DccTicketingCertificateSelectionAdapter
        .CertificatesItemVH<DccTicketingCertificateItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<DccTicketingCertificateItem> {

    override val asyncDiffer: AsyncDiffer<DccTicketingCertificateItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<
                    DccTicketingCertificateItem,
                    DccTicketingCertificateSelectionAdapter.CertificatesItemVH<DccTicketingCertificateItem, ViewBinding>
                    >(data),
                TypedVHCreatorMod({ data[it] is DccTicketingVaccinationCard.Item }) { DccTicketingVaccinationCard(it) },
                TypedVHCreatorMod({ data[it] is DccTicketingTestCard.Item }) { DccTicketingTestCard(it) },
                TypedVHCreatorMod({ data[it] is DccTicketingRecoveryCard.Item }) { DccTicketingRecoveryCard(it) }
            )
        )
    }

    override fun getItemCount(): Int = data.size
}
