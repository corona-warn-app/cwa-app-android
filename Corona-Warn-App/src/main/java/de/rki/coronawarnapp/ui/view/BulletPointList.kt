package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R

class BulletPointList(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private lateinit var entries: Array<CharSequence>

    init {
        orientation = VERTICAL

        context.withStyledAttributes(attrs, R.styleable.BulletPointList) {
            entries = getTextArray(R.styleable.BulletPointList_entries)
        }

        entries.forEachIndexed { i, entry ->
            // add point entry
            inflate(context, R.layout.view_bullet_point_entry, this)
            // set content
            this.getChildAt(i).findViewById<TextView>(R.id.bullet_point_content).text = entry
        }
    }
}
