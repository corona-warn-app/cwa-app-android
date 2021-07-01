package de.rki.coronawarnapp.ui.view

import android.text.InputFilter
import android.text.Spanned
import com.google.android.material.textfield.TextInputEditText
import java.lang.Character.OTHER_SYMBOL
import java.lang.Character.SURROGATE

/**
 * Adds [InputFilter] of emojis ,returns same [TextInputEditText]
 */
fun TextInputEditText.addEmojiFilter(): TextInputEditText {
    filters += EmojiFilter()
    return this
}

private class EmojiFilter : InputFilter {
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
            if (type in listOf(SURROGATE, OTHER_SYMBOL)) return ""
        }
        return null
    }
}
