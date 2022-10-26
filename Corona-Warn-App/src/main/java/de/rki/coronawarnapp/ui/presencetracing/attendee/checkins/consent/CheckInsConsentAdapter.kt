package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

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

class CheckInsConsentAdapter :
    ModularAdapter<CheckInsConsentAdapter.ItemVH<CheckInsConsentItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<CheckInsConsentItem> {

    override val asyncDiffer: AsyncDiffer<CheckInsConsentItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<CheckInsConsentItem, ItemVH<CheckInsConsentItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is HeaderCheckInsVH.Item }) { HeaderCheckInsVH(it) },
                TypedVHCreatorMod({ data[it] is SelectableCheckInVH.Item }) { SelectableCheckInVH(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : CheckInsConsentItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
