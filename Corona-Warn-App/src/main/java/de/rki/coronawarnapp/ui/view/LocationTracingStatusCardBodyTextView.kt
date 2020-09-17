package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.setUrl

class LocationTracingStatusCardBodyTextView : AppCompatTextView {

    constructor(context: Context?) : super(context) {
        setUrl()
    }

    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs) {
        setUrl()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        setUrl()
    }

    private fun setUrl() {
        setUrl(
            R.string.settings_tracing_status_location_body,
            "FAQ",
            context.getString(R.string.settings_tracing_status_location_body_url)
        )
    }
}
