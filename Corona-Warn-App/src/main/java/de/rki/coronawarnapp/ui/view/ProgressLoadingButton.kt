package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
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
    lateinit var defaultButton: MaterialButton
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

            val loading = getBoolean(R.styleable.ProgressLoadingButton_isLoading, false)
            defaultText = getText(R.styleable.ProgressLoadingButton_buttonText)?.toString() ?: ""
            binding.apply {
                defaultButton.text = defaultText
                isLoading = loading
            }
        }
    }

    var isLoading: Boolean = false
        set(value) {
            if (isActive) {
                setLoadingState(value)
                field = value
            }
        }

    var isActive: Boolean = true
        set(value) {
            if (!value) {
                setLoadingState(false)
            }
            binding.apply {
                isEnabled = value
                defaultButton.isEnabled = value
            }
            field = value
        }

    private fun setLoadingState(loading: Boolean) {
        binding.apply {
            isClickable = !loading
            defaultButton.isClickable = !loading
            isEnabled = !loading
            defaultButton.isEnabled = !loading
            defaultButton.text = if (loading) "" else defaultText
            progressIndicator.isVisible = loading
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        defaultButton.setOnClickListener(l)
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
