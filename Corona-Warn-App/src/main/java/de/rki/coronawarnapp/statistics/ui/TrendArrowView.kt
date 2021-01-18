package de.rki.coronawarnapp.statistics.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
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
) : FrameLayout(context, attrs, defStyleAttr) {

    private val imageView: ImageView

    init {
        LayoutInflater.from(context)
            .inflate(R.layout.statistics_trend_view, this, true)
        imageView = findViewById(R.id.trend)
    }

    fun setTrend(
        trend: KeyFigureCardOuterClass.KeyFigure.Trend,
        trendSemantic: KeyFigureCardOuterClass.KeyFigure.TrendSemantic
    ) {
        with(imageView) {
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

            contentDescription = context.getString(
                when (trend) {
                    INCREASING -> R.string.statistics_trend_increasing
                    DECREASING -> R.string.statistics_trend_decreasing
                    else -> R.string.statistics_trend_stable
                }
            )
        }
    }
}
