package de.rki.coronawarnapp.vaccination.ui.list.adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListCertificateCardItemVH
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListCertificateCardItemVH.VaccinationListCertificateCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListIncompleteTopCardItemVH
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListIncompleteTopCardItemVH.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem

class VaccinationListAdapter :
    ModularAdapter<VaccinationListAdapter.ItemVH<VaccinationListItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<VaccinationListItem> {

    override val asyncDiffer: AsyncDiffer<VaccinationListItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<VaccinationListItem, ItemVH<VaccinationListItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is VaccinationListIncompleteTopCardItem }) {
                    VaccinationListIncompleteTopCardItemVH(it)
                },
                TypedVHCreatorMod({ data[it] is VaccinationListNameCardItem }) {
                    VaccinationListNameCardItemVH(it)
                },
                TypedVHCreatorMod({ data[it] is VaccinationListVaccinationCardItem }) {
                    VaccinationListVaccinationCardItemVH(it)
                },
                TypedVHCreatorMod({ data[it] is VaccinationListCertificateCardItem }) {
                    VaccinationListCertificateCardItemVH(it)
                }
            )
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    abstract class ItemVH<Item : VaccinationListItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}

interface VaccinationListItem : HasStableId
