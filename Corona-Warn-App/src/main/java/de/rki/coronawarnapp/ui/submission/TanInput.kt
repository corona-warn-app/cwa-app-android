package de.rki.coronawarnapp.ui.submission

import android.content.Context
import android.os.Handler
import android.text.InputFilter
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.submission.TanConstants.MAX_LENGTH
import de.rki.coronawarnapp.util.TanHelper
import kotlinx.android.synthetic.main.view_tan_input_edittext.view.tan_input_edittext
import java.util.Locale
import kotlin.math.max

class TanInput(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    companion object {
        private const val KEYBOARD_TRIGGER_DELAY = 100L
        private const val EMPTY_STRING = ""
    }

    private val whitespaceFilter =
        InputFilter { source, _, _, _, _, _ -> source.filter { !it.isWhitespace() } }
    private val alphaNumericFilter = InputFilter { source, _, _, _, _, _ ->
        source.filter {
            TanConstants.ALPHA_NUMERIC_CHARS.contains(it)
        }
    }
    private var lengthFilter = InputFilter.LengthFilter(MAX_LENGTH)

    var listener: ((String?) -> Unit)? = null

    private var tan: String? = null

    private val lineSpacing: Int

    init {
        // add "hidden" edittext for input handling
        inflate(context, R.layout.view_tan_input_edittext, this)

        // add 3 blocks of digits
        inflate(context, R.layout.view_tan_input_group_3, this)
        inflate(context, R.layout.view_tan_input_group_3, this)
        inflate(context, R.layout.view_tan_input_group_4, this)

        lineSpacing = context.resources.getDimension(R.dimen.submission_tan_line_spacing).toInt()

        tan_input_edittext.filters = arrayOf(whitespaceFilter, alphaNumericFilter, lengthFilter)

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

    private fun limitLength(length: Int?) {
        lengthFilter = InputFilter.LengthFilter(if (length != null) length else MAX_LENGTH)
        tan_input_edittext.filters = arrayOf(whitespaceFilter, alphaNumericFilter, lengthFilter)
    }

    private fun updateTan(text: CharSequence?) {
        this.tan = text?.toString()?.toUpperCase(Locale.ROOT)
        updateDigits()
        tan?.let {
            limitLength(
                if (TanHelper.allCharactersValid(it)) null
                else it.length
            )
        }
        notifyListener()
    }

    private fun notifyListener() = listener?.invoke(tan)

    // returns all digits
    private fun digits(): List<TextView> = children
        .filterIsInstance(LinearLayout::class.java) // filter to 3 groups, ignoring edittext
        .flatMap {
            it.children.filterIsInstance(TextView::class.java) // ignore separators
        }
        .toList()

    private fun updateDigits() = digits().forEachIndexed { i, tanDigit ->
        val text = digitAtIndex(i)
        tanDigit.text = text
        tanDigit.background = when {
            text == EMPTY_STRING -> resources.getDrawable(R.drawable.tan_input_digit, null)
            TanHelper.isTanCharacterValid(text) -> resources.getDrawable(
                R.drawable.tan_input_digit_entered,
                null
            )
            else -> resources.getDrawable(R.drawable.tan_input_digit_error, null)
        }

        tanDigit.setTextColor(
            if (TanHelper.isTanCharacterValid(text))
                resources.getColor(R.color.colorTextPrimary1, null)
            else
                resources.getColor(R.color.colorTextSemanticRed, null)
        )
    }

    private fun digitAtIndex(index: Int): String = tan?.getOrNull(index)?.toString() ?: EMPTY_STRING

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val availableWith = MeasureSpec.getSize(widthMeasureSpec)

        var lines = 1
        var remainingWidthInLine = availableWith
        var longestLineWidth = 0

        // calculate number of lines and longest line
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)

            if (child is EditText) {
                // no need to place "hidden" input in line
                continue
            }

            // is there sill enough place at current line?
            if (child.measuredWidth <= remainingWidthInLine) {
                // child can be placed at current line
                remainingWidthInLine -= child.measuredWidth
                longestLineWidth = max(longestLineWidth, availableWith - remainingWidthInLine)
            } else {
                // child needs to be placed at next line
                lines++
                remainingWidthInLine = availableWith - child.measuredWidth
                longestLineWidth = max(longestLineWidth, availableWith - remainingWidthInLine)
            }
        }

        // calculate height assuming all groups have same height
        val childrenHeight = children.first { it is LinearLayout }.measuredHeight
        val resultingHeight = lines * childrenHeight + (lineSpacing * (lines - 1))

        val resultingWidth = longestLineWidth

        setMeasuredDimension(resultingWidth, resultingHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val availableWidth = right - left
        var remainingWidthInLine = availableWidth
        var currentLineStart = 0

        for (i in 0 until childCount) {
            val child = getChildAt(i)

            if (child is EditText) {
                // layout "hidden" edittext
                child.layout(0, 0, 1, 1)
                continue
            }

            val childWidth = child.measuredWidth

            // if not enough space in current line, target next
            if (childWidth > remainingWidthInLine) {
                currentLineStart += (child.measuredHeight + lineSpacing)
                remainingWidthInLine = availableWidth
            }

            // layout child at line
            val childTop = currentLineStart
            val childBottom = currentLineStart + child.measuredHeight
            val childStart = availableWidth - remainingWidthInLine
            val childEnd = childStart + childWidth
            remainingWidthInLine -= childWidth
            child.layout(childStart, childTop, childEnd, childBottom)
        }
    }
}
