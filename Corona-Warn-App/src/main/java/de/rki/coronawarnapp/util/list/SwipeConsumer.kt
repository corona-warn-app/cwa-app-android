package de.rki.coronawarnapp.util.list

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.util.lists.HasStableId
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * [RecyclerView] item data model should implement this contract to consume the swipe gestures
 */
interface SwipeConsumer {
    /**
     * On swipe callback
     * @param position [Int] item position
     * @param direction [Int] from [ItemTouchHelper] such as [ItemTouchHelper.RIGHT]
     */
    fun onSwipe(position: Int, direction: Int)
}

/**
 * returns true if the item is as [SwipeConsumer] , false otherwise
 * and helps with smart cast
 */

@OptIn(ExperimentalContracts::class)
fun HasStableId?.isSwipeable(): Boolean {
    contract {
        returns(true) implies (this@isSwipeable is SwipeConsumer)
    }
    return this != null && this is SwipeConsumer
}

/**
 * Indicates whether [RecyclerView.ViewHolder]'s can be moved (Dragged, Swiped) or not
 * by default movementFlags = ACTION_STATE_IDLE which indicates that the view does not move
 * this behaviour can be overridden and provide any flag from [ItemTouchHelper]
 */
interface Movable {
    val movementFlags: Int get() = ItemTouchHelper.ACTION_STATE_IDLE
}

@OptIn(ExperimentalContracts::class)
fun RecyclerView.ViewHolder?.isMovable(): Boolean {
    contract {
        returns(true) implies (this@isMovable is Movable)
    }
    return this != null && this is Movable
}
