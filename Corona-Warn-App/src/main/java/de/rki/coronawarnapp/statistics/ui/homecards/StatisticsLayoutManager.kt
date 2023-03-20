package de.rki.coronawarnapp.statistics.ui.homecards

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * A custom RecyclerView.LayoutManager implementation that extends
 * the height of items to match RecyclerView. 
 **/
class StatisticsLayoutManager(
    context: Context?,
    @RecyclerView.Orientation orientation: Int,
    reverseLayout: Boolean,
    private val fontScale: Float
) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return spanLayoutSize(super.generateDefaultLayoutParams())
    }

    override fun generateLayoutParams(c: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
        return spanLayoutSize(super.generateLayoutParams(c, attrs))
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return spanLayoutSize(super.generateLayoutParams(lp))
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        if (lp != null) {
            if (fontScale > 1.0F) {
                lp.height = (HEIGHT_MULTIPLIER * fontScale).roundToInt()
            }
        }
        return true
    }

    private fun spanLayoutSize(layoutParams: RecyclerView.LayoutParams): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            layoutParams.width,
            RecyclerView.LayoutParams.MATCH_PARENT
        )
    }

    companion object {
        const val HEIGHT_MULTIPLIER = 1050
    }
}
