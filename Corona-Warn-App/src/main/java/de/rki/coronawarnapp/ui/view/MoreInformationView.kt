package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Checkable
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ViewMoreInformationBinding

class MoreInformationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), Checkable {

    private val binding: ViewMoreInformationBinding
    private var onToggle: ((MoreInformationView, Boolean) -> Unit)? = null
    private var toggleOnText: String? = null
    private var toggleOffText: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_more_information, this, true)

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
        binding = ViewMoreInformationBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.MoreInformationView) {

            val titleText = getText(R.styleable.MoreInformationView_titleText) ?: ""
            val subtitleText = getText(R.styleable.MoreInformationView_subtitleText) ?: ""
            val isTopDividerVisible = getBoolean(R.styleable.MoreInformationView_isTopDividerVisible, true)
            val isBottomDividerVisible = getBoolean(R.styleable.MoreInformationView_isBottomDividerVisible, true)
            val isToggleVisible = getBoolean(R.styleable.MoreInformationView_isToggleVisible, false)
            val isChecked = getBoolean(R.styleable.MoreInformationView_android_checked, false)

            toggleOnText = getText(R.styleable.MoreInformationView_toggleOnText)?.toString()
            toggleOffText = getText(R.styleable.MoreInformationView_toggleOffText)?.toString()

            binding.apply {
                topDivider.isVisible = isTopDividerVisible
                bottomDivider.isVisible = isBottomDividerVisible
            }

            binding.apply {
                setTitle(titleText.toString())
                setSubtitle(subtitleText.toString())
            }

            binding.toggle.apply {
                isGone = !isToggleVisible
                isFocusable = false
                isClickable = false
            }
            setChecked(isChecked, notify = false)
        }
        updateContentDescription()
    }

    private fun updateContentDescription() {
        val title = binding.titleElement.text
        val subtitle = binding.subtitleElement.text
        val isToggleVisible = binding.toggle.isVisible
        contentDescription = if (isToggleVisible) {
            "$title $subtitle"
        } else {
            "$title $subtitle"
        }
    }

    fun setTitle(@StringRes stringRes: Int) {
        setTitle(context.getString(stringRes))
    }

    fun setTitle(title: String) {
        binding.titleElement.apply {
            text = title
            isVisible = title.isNotEmpty()
        }
        updateContentDescription()
    }

    fun setSubtitle(@StringRes stringRes: Int) {
        setSubtitle(context.getString(stringRes))
    }

    fun setSubtitle(subtitle: String) {
        binding.subtitleElement.apply {
            text = subtitle
            isVisible = subtitle.isNotEmpty()
        }
        updateContentDescription()
    }

    override fun setChecked(checked: Boolean) {
        setChecked(checked, notify = false)
    }

    fun setChecked(checked: Boolean, notify: Boolean) {
        if (!binding.toggle.isVisible) return
        val before = isChecked

        binding.toggle.isChecked = checked

        toggleOnText?.let { onText ->
            toggleOffText?.let { offText ->
                setSubtitle(if (isChecked) onText else offText)
            }
        }

        updateContentDescription()

        if (before != isChecked && notify) onToggle?.invoke(this, isChecked)
    }

    override fun isChecked(): Boolean = binding.toggle.isChecked

    override fun toggle() {
        isChecked = !isChecked
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.toggle.isEnabled = enabled
    }

    override fun setOnClickListener(userListener: OnClickListener?) {
        if (userListener == null) {
            super.setOnClickListener(null)
        } else {
            super.setOnClickListener {
                setChecked(checked = !isChecked, notify = true)
                userListener.onClick(this)
            }
        }
    }

    fun setUserToggleListener(onToggle: ((MoreInformationView, Boolean) -> Unit)?) {
        this.onToggle = onToggle
        if (!hasOnClickListeners()) {
            super.setOnClickListener {
                setChecked(checked = !isChecked, notify = true)
            }
        }
    }
}
