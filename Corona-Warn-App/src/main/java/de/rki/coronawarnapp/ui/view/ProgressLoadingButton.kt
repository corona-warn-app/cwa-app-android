package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ViewProgressLoadingButtonBinding

class ProgressLoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewProgressLoadingButtonBinding
    private var defaultText: String = ""
    lateinit var defaultButton: Button
        private set

    init {
        LayoutInflater.from(context).inflate(R.layout.view_progress_loading_button, this, true)
        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
        binding = ViewProgressLoadingButtonBinding.bind(this)
        val parentLayout = this
        context.withStyledAttributes(attrs, R.styleable.ProgressLoadingButton) {

            defaultButton = MaterialButton(context, attrs, defStyleAttr).apply {
                id = R.id.default_button
                addView(this, 0)
                layoutParams = LayoutParams(LayoutParams.MATCH_CONSTRAINT, LayoutParams.WRAP_CONTENT)
                textAlignment = TEXT_ALIGNMENT_CENTER
                maxWidth = Int.MAX_VALUE
            }

            parentLayout.maxWidth = Int.MAX_VALUE

            ConstraintSet().apply {
                clone(parentLayout)
                connect(defaultButton.id, ConstraintSet.TOP, parentLayout.id, ConstraintSet.TOP)
                connect(defaultButton.id, ConstraintSet.START, parentLayout.id, ConstraintSet.START)
                connect(defaultButton.id, ConstraintSet.END, parentLayout.id, ConstraintSet.END)
                connect(defaultButton.id, ConstraintSet.BOTTOM, parentLayout.id, ConstraintSet.BOTTOM)
                applyTo(parentLayout)
            }

            val loadingText = getText(R.styleable.ProgressLoadingButton_loadingText) ?: ""
            val loading = getBoolean(R.styleable.ProgressLoadingButton_isLoading, false)
            defaultText = getText(R.styleable.ProgressLoadingButton_buttonText).toString()
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
                defaultButton.isClickable = !value
                loadingButtonContainer.isVisible = value
                defaultButton.text = if (value) "" else defaultText
            }
            field = value
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
