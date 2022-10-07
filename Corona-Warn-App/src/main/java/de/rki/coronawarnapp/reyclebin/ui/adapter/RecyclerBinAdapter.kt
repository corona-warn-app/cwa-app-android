package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class RecyclerBinAdapter :
    ModularAdapter<RecyclerBinAdapter.ItemVH<RecyclerBinItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<RecyclerBinItem> {

    override val asyncDiffer: AsyncDiffer<RecyclerBinItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<RecyclerBinItem, ItemVH<RecyclerBinItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is VaccinationCertificateCard.Item }) { VaccinationCertificateCard(it) },
                TypedVHCreatorMod({ data[it] is TestCertificateCard.Item }) { TestCertificateCard(it) },
                TypedVHCreatorMod({ data[it] is RecoveryCertificateCard.Item }) { RecoveryCertificateCard(it) },
                TypedVHCreatorMod({ data[it] is CoronaTestCard.Item }) { CoronaTestCard(it) },
                TypedVHCreatorMod({ data[it] is OverviewSubHeaderItem }) { OverviewSubHeaderVH(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : RecyclerBinItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
