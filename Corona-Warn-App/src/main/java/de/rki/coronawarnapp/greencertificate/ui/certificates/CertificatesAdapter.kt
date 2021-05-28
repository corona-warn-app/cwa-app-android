package de.rki.coronawarnapp.greencertificate.ui.certificates

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod
import de.rki.coronawarnapp.vaccination.ui.homecard.CreateVaccinationHomeCard
import de.rki.coronawarnapp.vaccination.ui.homecard.ImmuneVaccinationHomeCard
import de.rki.coronawarnapp.vaccination.ui.homecard.VaccinationHomeCard

class CertificatesAdapter :
    ModularAdapter<CertificatesAdapter.CertificatesItemVH<CertificatesItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<CertificatesItem> {

    override val asyncDiffer: AsyncDiffer<CertificatesItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<CertificatesItem, CertificatesItemVH<CertificatesItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is ImmuneVaccinationHomeCard.Item }) {
                    ImmuneVaccinationHomeCard(it)
                },
                TypedVHCreatorMod({ data[it] is VaccinationHomeCard.Item }) {
                    VaccinationHomeCard(it)
                },
                TypedVHCreatorMod({ data[it] is CreateVaccinationHomeCard.Item }) { CreateVaccinationHomeCard(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class CertificatesItemVH<Item : CertificatesItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
