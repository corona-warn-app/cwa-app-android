package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.SettingsRowLayoutBinding

class SettingsRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: SettingsRowLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.settings_row_layout, this, true)
        binding = SettingsRowLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.SettingsRowView) {
            val title = getText(R.styleable.SettingsRowView_android_title) ?: ""
            binding.settingsRowHeaderTitle.isVisible = title.isNotEmpty()
            binding.settingsRowHeaderTitle.text = title

            val titleColor = getColor(
                R.styleable.SettingsRowView_android_textColor,
                resources.getColor(R.color.colorTextPrimary2, resources.newTheme())
            )
            binding.settingsRowHeaderTitle.setTextColor(titleColor)

            val subtitle = getText(R.styleable.SettingsRowView_android_subtitle) ?: ""
            binding.settingsRowSubtitle.text = subtitle

            val body = getText(R.styleable.SettingsRowView_settingsBody) ?: ""
            binding.settingsRowBody.isVisible = body.isNotEmpty()
            binding.settingsRowBody.text = body
        }
    }

    fun configureSettingsRowIcon(drawable: Drawable?, color: Int) {
        drawable?.setTint(color)
        binding.settingsRowIcon.setImageDrawable(drawable)
    }

    fun configureSettingsRowSubtitle(text: String) {
        binding.settingsRowSubtitle.isVisible = text.isNotEmpty()
        binding.settingsRowSubtitle.text = text
    }
}
