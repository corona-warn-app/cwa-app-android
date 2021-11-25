package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionAdapter.CertificatesItemVH
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class DccTicketingCertificateSelectionAdapter :
    ModularAdapter<CertificatesItemVH<DccTicketingCertificateItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<DccTicketingCertificateItem> {

    override val asyncDiffer: AsyncDiffer<DccTicketingCertificateItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<DccTicketingCertificateItem,
                    CertificatesItemVH<DccTicketingCertificateItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is DccTicketingVaccinationCard.Item }) { DccTicketingVaccinationCard(it) },
                TypedVHCreatorMod({ data[it] is DccTicketingTestCard.Item }) { DccTicketingTestCard(it) },
                TypedVHCreatorMod({ data[it] is DccTicketingRecoveryCard.Item }) { DccTicketingRecoveryCard(it) },
                TypedVHCreatorMod({ data[it] is DccTicketingNoValidCertificateCard.Item }) {
                    DccTicketingNoValidCertificateCard(
                        it
                    )
                },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class CertificatesItemVH<Item : DccTicketingCertificateItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup,
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
