package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.BulletpointDataPrivacyLayoutBinding

class BulletPointDataPrivacyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: BulletpointDataPrivacyLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.bulletpoint_data_privacy_layout, this, true)
        binding = BulletpointDataPrivacyLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.BulletPointDataPrivacyView) {

            val title = getText(R.styleable.BulletPointDataPrivacyView_android_title) ?: ""
            binding.bulletpointDataPrivacyTitle.isVisible = title.isNotEmpty()
            binding.bulletpointDataPrivacyTitle.text = title

            val subtitle = getText(R.styleable.BulletPointDataPrivacyView_android_subtitle) ?: ""
            binding.bulletpointDataPrivacySubtitle.isVisible = subtitle.isNotEmpty()
            binding.bulletpointDataPrivacySubtitle.text = subtitle

            val paragraph = getText(R.styleable.BulletPointDataPrivacyView_midSectionParagraph) ?: ""
            binding.midSectionParagraph.isVisible = paragraph.isNotEmpty()
            binding.midSectionParagraph.text = paragraph

            val secondParagraph = getText(R.styleable.BulletPointDataPrivacyView_secondParagraph) ?: ""
            binding.secondParagraph.isVisible = secondParagraph.isNotEmpty()
            binding.secondParagraph.text = secondParagraph

            val bulletpointOne = getText(R.styleable.BulletPointDataPrivacyView_bulletpointOne) ?: ""
            binding.bulletpointTextOne.isVisible = bulletpointOne.isNotEmpty()
            binding.bulletpointTextOne.setBulletPointText(bulletpointOne)

            val bulletpointTwo = getText(R.styleable.BulletPointDataPrivacyView_bulletpointTwo) ?: ""
            binding.bulletpointTextTwo.isVisible = bulletpointTwo.isNotEmpty()
            binding.bulletpointTextTwo.setBulletPointText(bulletpointTwo)

            val bulletPointThree = getText(R.styleable.BulletPointDataPrivacyView_bulletpointThree) ?: ""
            binding.bulletpointTextThree.isVisible = bulletPointThree.isNotEmpty()
            binding.bulletpointTextThree.setBulletPointText(bulletPointThree)

            val bulletPointFour = getText(R.styleable.BulletPointDataPrivacyView_bulletpointFour) ?: ""
            binding.bulletpointTextFour.isVisible = bulletPointFour.isNotEmpty()
            binding.bulletpointTextFour.setBulletPointText(bulletPointFour)

            val bulletPointFive = getText(R.styleable.BulletPointDataPrivacyView_bulletpointFive) ?: ""
            binding.bulletpointTextFive.isVisible = bulletPointFive.isNotEmpty()
            binding.bulletpointTextFive.setBulletPointText(bulletPointFive)

            val bulletPointSix = getText(R.styleable.BulletPointDataPrivacyView_bulletpointSix) ?: ""
            binding.bulletpointTextSix.isVisible = bulletPointSix.isNotEmpty()
            binding.bulletpointTextSix.setBulletPointText(bulletPointSix)

            val bulletPointSeven = getText(R.styleable.BulletPointDataPrivacyView_bulletpointSeven) ?: ""
            binding.bulletpointTextSeven.isVisible = bulletPointSeven.isNotEmpty()
            binding.bulletpointTextSeven.setBulletPointText(bulletPointSeven)

            val bulletPointEight = getText(R.styleable.BulletPointDataPrivacyView_bulletpointEight) ?: ""
            binding.bulletpointTextEight.isVisible = bulletPointEight.isNotEmpty()
            binding.bulletpointTextEight.setBulletPointText(bulletPointEight)

            val extraPcrBulletPoint = getText(R.styleable.BulletPointDataPrivacyView_extraPcrBulletpoint) ?: ""
            binding.extraPcrBulletpoint.isVisible = extraPcrBulletPoint.isNotEmpty()
            binding.extraPcrBulletpoint.setBulletPointText(extraPcrBulletPoint)
        }
    }

    fun showExtraBulletPoint(show: Boolean) {
        binding.extraPcrBulletpoint.isVisible = show
    }
}
