package de.rki.coronawarnapp.bugreporting

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ViewMoreInformationBinding

class MoreInformationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewMoreInformationBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.view_more_information, this, true)

        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        binding = ViewMoreInformationBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.MoreInformationView) {

            val titleText = getText(R.styleable.MoreInformationView_titleText) ?: ""
            val subtitleText = getText(R.styleable.MoreInformationView_subtitleText) ?: ""
            val isTopDividerVisible = getBoolean(R.styleable.MoreInformationView_isTopDividerVisible, true)

            binding.apply {

                topDivider.isVisible = isTopDividerVisible

                moreInformationTitle.text = titleText
                moreInformationTitle.isVisible = titleText.isNotEmpty()

                moreInformationSubtitle.text = subtitleText
                moreInformationSubtitle.isVisible = subtitleText.isNotEmpty()
            }
        }
    }

}
