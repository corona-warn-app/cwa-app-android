package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ViewProgressLoadingButtonBinding

class ProgressLoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewProgressLoadingButtonBinding
    var defaultButton: Button

    init {
        LayoutInflater.from(context).inflate(R.layout.view_progress_loading_button, this, true)
        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
        binding = ViewProgressLoadingButtonBinding.bind(this)
        defaultButton = binding.defaultButton

        context.withStyledAttributes(attrs, R.styleable.ProgressLoadingButton) {

            val defaultText = getText(R.styleable.ProgressLoadingButton_textWhileNotLoading) ?: ""
            val loadingText = getText(R.styleable.ProgressLoadingButton_textWhileLoading) ?: ""
            val isLoading = getBoolean(R.styleable.ProgressLoadingButton_isLoading, false)

            binding.apply {
                defaultButton.text = defaultText
                loadingButton.text = loadingText
                isLoading(isLoading)
            }
        }
    }

    fun isLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                defaultButton.visibility = GONE;
                loadingButtonContainer.visibility = VISIBLE
                isClickable = false
            } else {
                defaultButton.visibility = VISIBLE;
                loadingButtonContainer.visibility = GONE
                isClickable = true
            }
        }
    }
}
