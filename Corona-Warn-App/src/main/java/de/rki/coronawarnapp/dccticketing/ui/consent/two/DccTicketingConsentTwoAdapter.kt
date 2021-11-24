package de.rki.coronawarnapp.dccticketing.ui.consent.two

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.dccticketing.ui.consent.two.items.CertificateItem
import de.rki.coronawarnapp.dccticketing.ui.consent.two.items.RecoveryCertificateCard
import de.rki.coronawarnapp.dccticketing.ui.consent.two.items.TestCertificateCard
import de.rki.coronawarnapp.dccticketing.ui.consent.two.items.VaccinationCertificateCard
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class DccTicketingConsentTwoAdapter :
    ModularAdapter<DccTicketingConsentTwoAdapter.DccConsentTwoItemVH<CertificateItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<CertificateItem> {

    override val asyncDiffer: AsyncDiffer<CertificateItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<CertificateItem, DccConsentTwoItemVH<CertificateItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is VaccinationCertificateCard.Item }) { VaccinationCertificateCard(it) },
                TypedVHCreatorMod({ data[it] is TestCertificateCard.Item }) { TestCertificateCard(it) },
                TypedVHCreatorMod({ data[it] is RecoveryCertificateCard.Item }) { RecoveryCertificateCard(it) }
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class DccConsentTwoItemVH<Item : CertificateItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
