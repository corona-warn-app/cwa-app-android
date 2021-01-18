package de.rki.coronawarnapp.statistics.ui

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass.KeyFigure.Trend.DECREASING
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass.KeyFigure.Trend.INCREASING
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass.KeyFigure.TrendSemantic.NEGATIVE
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass.KeyFigure.TrendSemantic.POSITIVE

class TrendArrowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        scaleType = ScaleType.CENTER
        setTrend(INCREASING, NEGATIVE)
    }

    fun setTrend(
        trend: KeyFigureCardOuterClass.KeyFigure.Trend,
        trendSemantic: KeyFigureCardOuterClass.KeyFigure.TrendSemantic
    ) {
        rotation = when (trend) {
            INCREASING -> -45F
            DECREASING -> 45F
            else -> 0F
        }

        background = ContextCompat.getDrawable(
            context, when (trendSemantic) {
                POSITIVE -> R.drawable.bg_statistics_trend_positive
                NEGATIVE -> R.drawable.bg_statistics_trend_negative
                else -> R.drawable.bg_statistics_trend_neutral
            }
        )
    }
}
