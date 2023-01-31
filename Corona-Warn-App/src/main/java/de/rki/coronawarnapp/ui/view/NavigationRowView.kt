package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.NavigationRowLayoutBinding

class NavigationRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: NavigationRowLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.navigation_row_layout, this, true)
        binding = NavigationRowLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.NavigationRowView) {

            val icon = getResourceId(R.styleable.NavigationRowView_android_icon, 0)
            binding.navigationRowIcon.isVisible = icon != 0
            if (icon != 0) {
                val drawable = getDrawable(context, icon)
                binding.navigationRowIcon.setImageDrawable(drawable)
            }

            val title = getText(R.styleable.NavigationRowView_android_title) ?: ""
            binding.navigationRowTitle.isVisible = title.isNotEmpty()
            binding.navigationRowTitle.text = title

            val subtitle = getText(R.styleable.NavigationRowView_android_subtitle) ?: ""
            binding.navigationRowSubtitle.isVisible = subtitle.isNotEmpty()
            binding.navigationRowSubtitle.text = subtitle
        }
    }
}
