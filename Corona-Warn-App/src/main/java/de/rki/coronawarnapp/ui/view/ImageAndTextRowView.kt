package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ImageAndTextRowLayoutBinding

class ImageAndTextRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: ImageAndTextRowLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.image_and_text_row_layout, this, true)
        binding = ImageAndTextRowLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.ImageAndTextRowView) {
            with(binding) {

                val image = getInteger(R.styleable.ImageAndTextRowView_android_src, 0)
                if (image == 0) {
                    rowIcon.isVisible = true
                } else {
                    val drawable = getDrawable(context, image)
                    rowIcon.isVisible = true
                    rowIcon.setImageDrawable(drawable)
                }

                val text = getText(R.styleable.ImageAndTextRowView_android_description) ?: ""
                rowText.text = text
            }
        }
    }
}
