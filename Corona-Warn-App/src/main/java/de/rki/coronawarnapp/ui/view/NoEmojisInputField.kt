package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText

class NoEmojisInputField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextInputEditText(context, attrs, defStyleAttr)


class EmojiFilter : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        for (index in start until end) {
            val type = Character.getType(source[index]).toByte()
            if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                return ""
            }
        }
        return null
    }
}
