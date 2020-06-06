package de.rki.coronawarnapp.ui.submission

import android.content.Context
import android.os.Handler
import android.text.InputFilter
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.widget.doOnTextChanged
import de.rki.coronawarnapp.R
import java.util.Locale
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_edittext
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_1
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_2
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_3
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_4
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_5
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_6
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_7
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_8
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_9
import kotlinx.android.synthetic.main.view_tan_input.view.tan_input_textview_10
import kotlinx.android.synthetic.main.view_tan_input.view.dash_view_1
import kotlinx.android.synthetic.main.view_tan_input.view.dash_view_2

class TanInput(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    companion object {
        private const val KEYBOARD_TRIGGER_DELAY = 100L
    }

    private val whitespaceFilter =
        InputFilter { source, _, _, _, _, _ -> source.filter { !it.isWhitespace() } }
    private val alphaNumericFilter = InputFilter { source, _, _, _, _, _ ->
        source.filter {
            TanConstants.ALPHA_NUMERIC_CHARS.contains(it)
        }
    }
    private val lengthFilter = InputFilter.LengthFilter(TanConstants.MAX_LENGTH)

    var listener: ((String?) -> Unit)? = null

    private var tan: String? = null

    init {
        inflate(context, R.layout.view_tan_input, this)

        tan_input_edittext.filters = arrayOf(whitespaceFilter, alphaNumericFilter, lengthFilter)

        dash_view_1.text = "-"
        dash_view_2.text = "-"

        // register listener
        tan_input_edittext.doOnTextChanged { text, _, _, _ -> updateTan(text) }
        setOnClickListener { showKeyboard() }

        // initially show the keyboard
        Handler().postDelayed({ showKeyboard() }, KEYBOARD_TRIGGER_DELAY)
    }

    private fun showKeyboard() {
        if (tan_input_edittext.requestFocus()) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(tan_input_edittext, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun updateTan(text: CharSequence?) {
        this.tan = text?.toString()?.toUpperCase(Locale.getDefault())
        updateDigits()
        notifyListener()
    }

    private fun notifyListener() = listener?.invoke(tan)

    private fun updateDigits() = listOf(
        tan_input_textview_1,
        tan_input_textview_2,
        tan_input_textview_3,
        tan_input_textview_4,
        tan_input_textview_5,
        tan_input_textview_6,
        tan_input_textview_7,
        tan_input_textview_8,
        tan_input_textview_9,
        tan_input_textview_10
    ).forEachIndexed { i, tanDigit ->
        tanDigit.text = digitAtIndex(i)
    }

    private fun digitAtIndex(index: Int): String = tan?.getOrNull(index)?.toString() ?: ""
}
