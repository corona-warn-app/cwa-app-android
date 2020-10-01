package de.rki.coronawarnapp.ui.lists

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : BaseAdapter.VH> : RecyclerView.Adapter<T>() {

    @CallSuper
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        return onCreateBaseVH(parent, viewType)
    }

    abstract fun onCreateBaseVH(parent: ViewGroup, viewType: Int): T

    @CallSuper
    final override fun onBindViewHolder(holder: T, position: Int) {
        onBindBaseVH(holder, position)
    }

    abstract fun onBindBaseVH(holder: T, position: Int)

    abstract class VH(@LayoutRes layoutRes: Int, parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    ) {

        val context: Context = parent.context

        fun getColor(@ColorRes colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

        fun getString(@StringRes stringRes: Int, vararg args: Any): String =
            context.getString(stringRes, *args)
    }
}
