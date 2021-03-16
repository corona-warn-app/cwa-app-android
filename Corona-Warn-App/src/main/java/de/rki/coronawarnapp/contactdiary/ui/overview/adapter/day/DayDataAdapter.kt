package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact.ContactVH
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.header.HeaderItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.header.HeaderVH
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskcalculated.RiskCalculatedItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.riskcalculated.RiskCalculatedVH
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class DayDataAdapter :
    ModularAdapter<DayDataAdapter.ItemVH<DayDataItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<DayDataItem> {

    override val asyncDiffer: AsyncDiffer<DayDataItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<DayDataItem, ItemVH<DayDataItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is HeaderItem }) { HeaderVH(it) },
                TypedVHCreatorMod({ data[it] is RiskCalculatedItem }) { RiskCalculatedVH(it) },
                TypedVHCreatorMod({ data[it] is ContactItem }) { ContactVH(it) }
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : DayDataItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
