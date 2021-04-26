package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items

import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsAdapter

abstract class BaseCheckInVH<ItemT : CheckInsItem, BindingT : ViewBinding>(
    parent: ViewGroup,
    @LayoutRes layoutRes: Int
) : CheckInsAdapter.ItemVH<ItemT, BindingT>(
    layoutRes = layoutRes,
    parent = parent
) {

    companion object {
        fun View.setupMenu(@MenuRes menuRes: Int, onMenuAction: (MenuItem) -> Boolean) {
            val menu = PopupMenu(context, this, Gravity.TOP or Gravity.END).apply {
                inflate(menuRes)
                setOnMenuItemClickListener { onMenuAction(it) }
            }
            setOnClickListener { menu.show() }
        }
    }
}
