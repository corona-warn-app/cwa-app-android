package de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListImmunityInformationCardItemVH
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListImmunityInformationCardItemVH.VaccinationListImmunityInformationCardItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListQrCodeCardItemVH
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListQrCodeCardItemVH.VaccinationListQrCodeCardItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListVaccinationCardItemVH.VaccinationListVaccinationCardItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class VaccinationListAdapter :
    ModularAdapter<VaccinationListAdapter.ItemVH<VaccinationListItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<VaccinationListItem> {

    override val asyncDiffer: AsyncDiffer<VaccinationListItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<VaccinationListItem, ItemVH<VaccinationListItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is VaccinationListNameCardItem }) {
                    VaccinationListNameCardItemVH(it)
                },
                TypedVHCreatorMod({ data[it] is VaccinationListImmunityInformationCardItem }) {
                    VaccinationListImmunityInformationCardItemVH(it)
                },
                TypedVHCreatorMod({ data[it] is VaccinationListVaccinationCardItem }) {
                    VaccinationListVaccinationCardItemVH(it)
                },
                TypedVHCreatorMod({ data[it] is VaccinationListQrCodeCardItem }) {
                    VaccinationListQrCodeCardItemVH(it)
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
