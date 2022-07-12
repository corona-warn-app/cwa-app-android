package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ViewProgressLoadingButtonBinding

class ProgressLoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewProgressLoadingButtonBinding
    private var defaultText: String = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.view_progress_loading_button, this, true)
        binding = ViewProgressLoadingButtonBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.ProgressLoadingButton) {
            val loading = getBoolean(R.styleable.ProgressLoadingButton_isLoading, false)
            defaultText = getText(R.styleable.ProgressLoadingButton_buttonText).toString()
            binding.apply {
                defaultButton.text = defaultText
                isLoading = loading
            }
        }
    }

    var isLoading: Boolean = false
        set(value) {
            if (isActive) {
                binding.apply {
                    defaultButton.isClickable = !value
                    defaultButton.text = if (value) "" else defaultText
                    binding.progressIndicator.isVisible = value
                    defaultButton.isPressed = value
                }
                field = value
            }
        }

    var isActive: Boolean = true
        set(value) {
            if (!value) {
                isLoading = false
            }
            binding.apply {
                isEnabled = value
                defaultButton.isEnabled = value
            }
            field = value
        }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.defaultButton.setOnClickListener(l)
    }

    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putParcelable(SUPER_STATE, super.onSaveInstanceState())
            putBoolean(LOADING_STATE, isLoading)
        }

    override fun onRestoreInstanceState(parcelable: Parcelable?) {
        var state = parcelable
        if (state is Bundle) {
            isLoading = state.getBoolean(LOADING_STATE, false)
            state = state.getParcelable(SUPER_STATE)
        }
        super.onRestoreInstanceState(state)
    }

    companion object {
        private const val SUPER_STATE = "superState"
        private const val LOADING_STATE = "loadingState"
    }
}
