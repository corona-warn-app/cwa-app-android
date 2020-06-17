package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class UnscrollableEditText(context: Context, attributeSet: AttributeSet) :
    AppCompatEditText(context, attributeSet) {
    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        text?.length?.let { setSelection(it) }
    }
}
