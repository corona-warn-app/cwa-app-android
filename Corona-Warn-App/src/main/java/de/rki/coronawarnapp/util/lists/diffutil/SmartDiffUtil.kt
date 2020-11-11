package de.rki.coronawarnapp.util.lists.diffutil

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.util.lists.HasStableId

interface AsyncDiffUtilAdapter<T : HasStableId> {

    val data: List<T>
        get() = asyncDiffer.currentList

    val asyncDiffer: AsyncDiffer<T>
}

fun <X, T> X.update(
    newData: List<T>?,
    notify: Boolean = true
) where X : AsyncDiffUtilAdapter<T>, X : RecyclerView.Adapter<*> {

    if (notify) asyncDiffer.submitUpdate(newData ?: emptyList())
}

class AsyncDiffer<T : HasStableId>(
    adapter: RecyclerView.Adapter<*>,
    compareItem: (T, T) -> Boolean = { i1, i2 -> i1.stableId == i2.stableId },
    compareItemContent: (T, T) -> Boolean = { i1, i2 -> i1 == i2 },
    determinePayload: (T, T) -> Any? = { i1, i2 ->
        when {
            i1 is HasPayloadDiffer && i1::class.java.isInstance(i2) -> i1.diffPayload(i1, i2)
            else -> null
        }
    }
) {
    private val callback = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = compareItem(oldItem, newItem)
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = compareItemContent(oldItem, newItem)
        override fun getChangePayload(oldItem: T, newItem: T): Any? = determinePayload(oldItem, newItem)
    }

    private val listDiffer = AsyncListDiffer(adapter, callback)
    private val internalList = mutableListOf<T>()
    val currentList: List<T>
        get() = synchronized(internalList) { internalList }

    init {
        adapter.setHasStableIds(true)
    }

    fun submitUpdate(newData: List<T>) {
        listDiffer.submitList(newData) {
            synchronized(internalList) {
                internalList.clear()
                internalList.addAll(newData)
            }
        }
    }
}
