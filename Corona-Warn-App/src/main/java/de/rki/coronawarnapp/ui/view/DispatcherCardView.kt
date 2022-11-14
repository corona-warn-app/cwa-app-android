package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DispatcherCardLayoutBinding

class DispatcherCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: DispatcherCardLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.dispatcher_card_layout, this, true)
        val outValue = TypedValue()
        getContext().theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
        binding = DispatcherCardLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.DispatcherCardView) {

            val headerText = getText(R.styleable.DispatcherCardView_headline) ?: ""
            binding.dispatcherCardTitle.text = headerText
            val bodyText = getText(R.styleable.DispatcherCardView_body) ?: ""
            binding.submissionDispatcherCardText.text = bodyText
            binding.submissionDispatcherCardText.contentDescription = bodyText
            val illustration = getResourceId(R.styleable.DispatcherCardView_illustration, 0)
            if (illustration != 0) {
                val drawable = getDrawable(context, illustration)
                binding.dispatcherCardIllustration.setImageDrawable(drawable)
            }
            val topIcon =
                getDrawable(context, getResourceId(R.styleable.DispatcherCardView_topIcon, R.drawable.ic_forward))
            binding.dispatcherCardIcon.setImageDrawable(topIcon)
            val cardBackground = getDrawable(
                context,
                getResourceId(R.styleable.DispatcherCardView_cardBackground, R.drawable.dispatcher_card_background)
            )
            val textColor = getColor(
                R.styleable.DispatcherCardView_colorForText,
                resources.getColor(R.color.colorOnPrimary, getContext().theme)
            )
            binding.dispatcherCard.background = cardBackground
            binding.dispatcherCardTitle.setTextColor(textColor)
            binding.submissionDispatcherCardText.setTextColor(textColor)
            binding.dispatcherCardIcon.setColorFilter(
                getColor(
                    R.styleable.DispatcherCardView_iconTint,
                    resources.getColor(
                        R.color.colorTextPrimary2, getContext().theme
                    )
                )
            )
        }
    }
}
