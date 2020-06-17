package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet

class UnscrollableEditText(context: Context, attributeSet: AttributeSet) :
    androidx.appcompat.widget.AppCompatEditText(context, attributeSet) {
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        text?.length?.let { setSelection(it) }
    }
}
