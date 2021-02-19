package de.rki.coronawarnapp.contactdiary.ui.day.tabs.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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

    private var afterTextChangedListener: ((String) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_diary_circumstances_textview, this, true)

        input = findViewById<EditText>(R.id.input).apply {
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    Timber.v("Focused on %s", v)
                } else {
                    Timber.v("Lost focus on %s", v)
                    afterTextChangedListener?.invoke(text.toString())
                }
            }
            // When the user entered something and puts the app into the background
            viewTreeObserver.addOnWindowFocusChangeListener {
                afterTextChangedListener?.invoke(text.toString())
            }
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
