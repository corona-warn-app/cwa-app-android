package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import de.rki.coronawarnapp.R
import setTextWithUrl

class LocationTracingStatusCardBodyTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        setTextWithUrl(
            R.string.settings_tracing_status_location_body,
            "FAQ",
            context.getString(R.string.settings_tracing_status_location_body_url)
        )
    }
}
