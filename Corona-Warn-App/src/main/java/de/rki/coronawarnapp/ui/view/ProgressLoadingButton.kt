package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
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

            val defaultText = getText(R.styleable.ProgressLoadingButton_buttonText) ?: ""
            val loadingText = getText(R.styleable.ProgressLoadingButton_loadingText) ?: ""
            val loading = getBoolean(R.styleable.ProgressLoadingButton_isLoading, false)

            binding.apply {
                defaultButton.text = defaultText
                loadingButton.text = loadingText
                isLoading = loading
            }
        }
    }

    var isLoading: Boolean = false
        set(value) {
            binding.apply {
                defaultButton.isGone = value
                loadingButtonContainer.isVisible = value
            }
            field = value
        }
}
