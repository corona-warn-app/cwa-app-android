package de.rki.coronawarnapp.contactdiary.ui.day.tabs.common

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R

class DiaryCircumstancesTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val input: EditText
    private val infoButton: ImageView

    private var afterTextChangedListener: ((String) -> Unit)? = null
    private val circumStanceTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // NOOP
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // NOOP
        }

        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            afterTextChangedListener?.invoke(s.toString())
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_diary_circumstances_textview, this, true)

        input = findViewById<EditText>(R.id.input).apply {
            addTextChangedListener(circumStanceTextWatcher)
        }
        infoButton = findViewById(R.id.info_button)
    }

    override fun onFinishInflate() {
        input.clearFocus()
        super.onFinishInflate()
    }

    fun setInfoButtonClickListener(listener: () -> Unit) {
        infoButton.setOnClickListener { listener() }
    }

    fun setInputTextChangedListener(listener: ((String) -> Unit)?) {
        afterTextChangedListener = listener
    }

    fun setInputText(text: String) {
        val temp = afterTextChangedListener
        afterTextChangedListener = null
        input.setText(text)
        afterTextChangedListener = temp
    }
}
