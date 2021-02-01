package de.rki.coronawarnapp.statistics.util

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Custom [LinearSnapHelper] to ensure also first and last item can be reached
 * @see <a href="https://stackoverflow.com/questions/52688974/recyclerview-snaphelper-fails-to-show-first-last-items/58876237#58876237">Stack Overflow</a>
 */
class CustomSnapHelper : LinearSnapHelper() {
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        val linearLayoutManager = layoutManager as? LinearLayoutManager ?: return super.findSnapView(layoutManager)

        return linearLayoutManager.takeIf { shouldSnap(it) }?.run { super.findSnapView(layoutManager) }
    }

    private fun shouldSnap(layoutManager: LinearLayoutManager) = with(layoutManager) {
        // Don't snap when the first completely visible item is also the first one of the list
        findFirstCompletelyVisibleItemPosition() != 0 &&
            // Don't snap when the last completely visible item is also the last one of the list
            findLastCompletelyVisibleItemPosition() != itemCount - 1
    }
}
