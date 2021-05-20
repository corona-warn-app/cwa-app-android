package de.rki.coronawarnapp.util.list

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * [RecyclerView] item data model should implement this contract to consume the swipe gestures
 */
interface Swipeable {
    /**
     * Indicates whether [RecyclerView.ViewHolder]'s can be moved (Dragged, Swiped) or not
     * by default movementFlags = ACTION_STATE_IDLE which indicates that the view does not move
     * this behaviour can be overridden and provide any flag from [ItemTouchHelper]
     */
    val movementFlags: Int? get() = null

    /**
     * On swipe callback
     * @param holder [RecyclerView.ViewHolder]  that was swiped
     * @param direction [Int] from [ItemTouchHelper] such as [ItemTouchHelper.RIGHT]
     */
    fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int)
}
