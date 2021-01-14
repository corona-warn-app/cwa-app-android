package de.rki.coronawarnapp.ui.lists

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : BaseAdapter.VH> : RecyclerView.Adapter<T>() {

    @CallSuper
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        return onCreateBaseVH(parent, viewType)
    }

    abstract fun onCreateBaseVH(parent: ViewGroup, viewType: Int): T

    @CallSuper
    final override fun onBindViewHolder(holder: T, position: Int) {
        onBindBaseVH(holder, position, mutableListOf())
    }

    @CallSuper
    final override fun onBindViewHolder(holder: T, position: Int, payloads: MutableList<Any>) {
        onBindBaseVH(holder, position, payloads)
    }

    abstract fun onBindBaseVH(holder: T, position: Int, payloads: MutableList<Any> = mutableListOf())

    abstract class VH(@LayoutRes layoutRes: Int, private val parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    ) {

        val context: Context
            get() = parent.context

        val resources: Resources
            get() = context.resources

        val layoutInflater: LayoutInflater
            get() = LayoutInflater.from(context)
    }
}
