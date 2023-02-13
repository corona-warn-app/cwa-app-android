package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.InformationDetailsLayoutBinding
import de.rki.coronawarnapp.util.formatter.parseHtmlFromAssets

class InformationDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: InformationDetailsLayoutBinding
    private var htmlTextPath = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.information_details_layout, this, true)
        binding = InformationDetailsLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.InformationDetailsView) {
            with(binding) {

                val title = getText(R.styleable.InformationDetailsView_android_title) ?: ""
                informationDetailsHeaderHeadline.isVisible = title.isNotEmpty()
                informationDetailsHeaderHeadline.text = title

                val subtitle = getText(R.styleable.InformationDetailsView_android_subtitle) ?: ""
                htmlTextPath = subtitle.toString()
                informationDetailsHeaderBody.isVisible = subtitle.isNotEmpty()
                informationDetailsHeaderBody.text = subtitle

                val contentDescription = getText(R.styleable.InformationDetailsView_android_contentDescription) ?: ""
                val image = getResourceId(R.styleable.InformationDetailsView_android_src, 0)
                if (image == 0) {
                    informationDetailsHeaderIllustration.isVisible = false
                } else {
                    val drawable = getDrawable(context, image)
                    informationDetailsHeaderIllustration.setImageDrawable(drawable)
                    informationDetailsHeaderIllustration.contentDescription = contentDescription
                }
            }
        }
    }

    fun getInformationDetailsHtml() {
        if (htmlTextPath.isNotEmpty()) {
            val bodyHtmlText = parseHtmlFromAssets(context, htmlTextPath)
            binding.informationDetailsHeaderBody.text = bodyHtmlText
        }
    }

    fun getInformationImageAndDescription(image: Drawable?, description: String) {
        with(binding) {
            if (image != null) {
                informationDetailsHeaderIllustration.isVisible = true
                informationDetailsHeaderIllustration.setImageDrawable(image)
            }
            informationDetailsHeaderIllustration.contentDescription = description
        }
    }
}
