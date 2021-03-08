package de.rki.coronawarnapp.contactdiary.ui.day.tabs.common

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R
import timber.log.Timber

class DiaryCircumstancesTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val input: EditText
    private val infoButton: ImageView
    private var lastSavedText: String? = null

    private var afterTextChangedListener: ((String) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_diary_circumstances_textview, this, true)

        input = findViewById<EditText>(R.id.input).apply {
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    Timber.v("Focused on %s", v)
                } else {
                    Timber.v("Lost focus on %s", v)
                    notifyTextChanged(text.toString())
                }
            }
            imeOptions = EditorInfo.IME_ACTION_DONE
            setRawInputType(InputType.TYPE_CLASS_TEXT)

            // When the user entered something and puts the app into the background
            viewTreeObserver.addOnWindowFocusChangeListener { windowFocus ->
                if (hasFocus() && !windowFocus) {
                    Timber.v("User has left app, input had focus, triggering notifyTextChanged")
                    notifyTextChanged(text.toString())
                }
            }
        }
        infoButton = findViewById(R.id.info_button)
    }

    override fun onFinishInflate() {
        input.clearFocus()
        super.onFinishInflate()
    }

    private fun notifyTextChanged(text: String) {
        if (lastSavedText == text) {
            Timber.v("New text equals last text, skipping notify.")
            return
        }
        // Prevent Copy&Paste inserting new lines.
        afterTextChangedListener?.let {
            it.invoke(text.trim().replace("\n", ""))
            lastSavedText = text
        }
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
