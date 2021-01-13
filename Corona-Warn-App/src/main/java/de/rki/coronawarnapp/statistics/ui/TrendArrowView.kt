package de.rki.coronawarnapp.statistics.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import timber.log.Timber

class TrendArrowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    fun setTrend(
        trend: KeyFigureCardOuterClass.KeyFigure.Trend,
        trendSemantic: KeyFigureCardOuterClass.KeyFigure.TrendSemantic
    ) {
        Timber.d("setTrend(trend=$trend, trendSemantic=$trendSemantic)")
    }
}
