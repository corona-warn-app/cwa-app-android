package de.rki.coronawarnapp.vaccination.ui.list

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListCertificateCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListCertificateCardItemVH
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListIncompleteTopCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListIncompleteTopCardItemVH
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListNameCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListNameCardItemVH
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListVaccinationCardItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListVaccinationCardItemVH

class VaccinationListAdapter(
    vaccinationListItems: List<VaccinationListItem>,
    private val onVaccinationClickListener: (vaccinationItem: VaccinationListVaccinationCardItem) -> Unit
) : ModularAdapter<VaccinationListAdapter.ItemVH<VaccinationListItem, ViewBinding>>(),
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
                    VaccinationListVaccinationCardItemVH(it, onVaccinationClickListener)
                },
                TypedVHCreatorMod({ data[it] is VaccinationListCertificateCardItem }) {
                    VaccinationListCertificateCardItemVH(it)
                }
            )
        )

        update(vaccinationListItems)
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
