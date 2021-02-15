package de.rki.coronawarnapp.contactdiary.ui.day.tabs.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ui.setGone

class ExpandingDiaryListItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val checkboxView: ImageView
    private val container: ViewGroup

    init {
        LayoutInflater.from(context).inflate(R.layout.view_expanding_diary_listitem, this, true)

        titleView = findViewById(R.id.header_title)
        checkboxView = findViewById(R.id.header_checkbox)
        container = findViewById(R.id.container)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        if (TOPLEVEL_IDS.contains(child.id)) {
            super.addView(child, index, params)
        } else {
            container.addView(child, index, params)
        }
    }

    var title: String
        get() = titleView.text.toString()
        set(value) {
            titleView.text = value
        }

    var isExpanded: Boolean
        get() = isSelected
        set(value) {
            checkboxView.setImageResource(if (value) R.drawable.ic_selected else R.drawable.ic_unselected)
            container.setGone(!value)
            isSelected = value
        }

    companion object {
        private val TOPLEVEL_IDS = listOf(
            R.id.header, R.id.divider, R.id.container
        )
    }
}
