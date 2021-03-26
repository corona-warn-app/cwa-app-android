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
