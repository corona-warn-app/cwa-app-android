package de.rki.coronawarnapp.util.lists.modular.mods

import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter

class StableIdMod<ItemT : HasStableId> constructor(
    private val data: List<ItemT>,
    private val customResolver: (position: Int) -> Long = {
        (data[it] as? HasStableId)?.stableId ?: RecyclerView.NO_ID
    }
) : ModularAdapter.Module.ItemId, ModularAdapter.Module.Setup {

    override fun onAdapterReady(adapter: ModularAdapter<*>) {
        adapter.setHasStableIds(true)
    }

    override fun getItemId(adapter: ModularAdapter<*>, position: Int): Long? {
        return customResolver.invoke(position)
    }
}
