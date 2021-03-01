package de.rki.coronawarnapp.util

import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun TextInputEditText.setTextOnTextInput(
    text: String?,
    endIconVisible: Boolean = true
) {
    this.setText(text)
    (parent?.parent as? TextInputLayout)?.isEndIconVisible = endIconVisible
}
