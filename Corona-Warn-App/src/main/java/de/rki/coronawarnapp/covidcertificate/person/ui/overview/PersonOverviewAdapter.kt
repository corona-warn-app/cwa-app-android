package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class PersonOverviewAdapter :
    ModularAdapter<PersonOverviewAdapter.PersonOverviewItemVH<CertificatesItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<CertificatesItem> {

    override val asyncDiffer: AsyncDiffer<CertificatesItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<CertificatesItem, PersonOverviewItemVH<CertificatesItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is CovidTestCertificatePendingCard.Item }) {
                    CovidTestCertificatePendingCard(it)
                },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class PersonOverviewItemVH<Item : CertificatesItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
