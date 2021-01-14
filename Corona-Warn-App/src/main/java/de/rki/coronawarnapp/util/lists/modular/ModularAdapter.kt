package de.rki.coronawarnapp.util.lists.modular

import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.ui.lists.BaseAdapter

abstract class ModularAdapter<VH : ModularAdapter.VH> : BaseAdapter<VH>() {
    val modules = mutableListOf<Module>()

    init {
        modules.filterIsInstance<Module.Setup>().forEach { it.onAdapterReady(this) }
    }

    override fun getItemId(position: Int): Long {
        modules.filterIsInstance<Module.ItemId>().forEach {
            val id = it.getItemId(this, position)
            if (id != null) return id
        }
        return super.getItemId(position)
    }

    @CallSuper
    override fun getItemViewType(position: Int): Int {
        modules.filterIsInstance<Module.Typing>().forEach {
            val type = it.onGetItemType(this, position)
            if (type != null) return type
        }
        return super.getItemViewType(position)
    }

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): VH {
        modules.filterIsInstance<Module.Creator<VH>>().forEach {
            val vh = it.onCreateModularVH(this, parent, viewType)
            if (vh != null) return vh
        }
        throw IllegalStateException("Couldn't create VH for type $viewType with $parent")
    }

    @CallSuper
    override fun onBindBaseVH(holder: VH, position: Int, payloads: MutableList<Any>) {
        modules.filterIsInstance<Module.Binder<VH>>().forEach {
            it.onBindModularVH(this, holder, position, payloads)
        }
    }

    abstract class VH(@LayoutRes layoutRes: Int, parent: ViewGroup) : BaseAdapter.VH(layoutRes, parent)

    interface Module {
        interface Setup {
            fun onAdapterReady(adapter: ModularAdapter<*>)
        }

        interface Creator<T : VH> : Module {
            fun onCreateModularVH(adapter: ModularAdapter<T>, parent: ViewGroup, viewType: Int): T?
        }

        interface Binder<T : VH> : Module {
            fun onBindModularVH(adapter: ModularAdapter<T>, vh: T, pos: Int, payloads: MutableList<Any>)
        }

        interface Typing : Module {
            fun onGetItemType(adapter: ModularAdapter<*>, pos: Int): Int?
        }

        interface ItemId : Module {
            fun getItemId(adapter: ModularAdapter<*>, position: Int): Long?
        }
    }
}
