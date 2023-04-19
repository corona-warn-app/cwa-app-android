package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.OnboardingScreensLayoutBinding
import de.rki.coronawarnapp.util.formatter.parseHtmlFromAssets

class OnboardingScreensView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: OnboardingScreensLayoutBinding
    private var htmlTextPath = ""

    init {
        LayoutInflater.from(context).inflate(R.layout.onboarding_screens_layout, this, true)
        binding = OnboardingScreensLayoutBinding.bind(this)

        context.withStyledAttributes(attrs, R.styleable.OnboardingScreensView) {
            val onboardingImage = getDrawable(
                context,
                getResourceId(
                    R.styleable.OnboardingScreensView_android_src,
                    R.drawable.ic_illustration_together
                )
            )
            binding.onboardingScreensIllustration.setImageDrawable(onboardingImage)

            val illustrationDescription = getText(R.styleable.OnboardingScreensView_illustrationDescription) ?: ""
            binding.onboardingScreensIllustration.contentDescription = illustrationDescription

            val onboardingImageDescription =
                getText(R.styleable.OnboardingScreensView_android_contentDescription)
            binding.onboardingScreensIllustration.contentDescription = onboardingImageDescription

            val titleText = getText(R.styleable.OnboardingScreensView_android_title) ?: ""
            binding.onboardingScreensHeadline.isGone = titleText.isEmpty()
            binding.onboardingScreensHeadline.text = titleText

            val subtitleText = getText(R.styleable.OnboardingScreensView_android_subtitle) ?: ""
            binding.onboardingScreensSubtitle.isGone = subtitleText.isEmpty()
            binding.onboardingScreensSubtitle.text = subtitleText

            val normalBodyText = getText(R.styleable.OnboardingScreensView_bodyNormal) ?: ""
            binding.onboardingScreensBody.isGone = normalBodyText.isEmpty()
            binding.onboardingScreensBody.text = normalBodyText

            val emphasizedBodyText = getText(R.styleable.OnboardingScreensView_bodyEmphasized) ?: ""
            binding.onboardingScreensBodyEmphasized.isGone = emphasizedBodyText.isEmpty()
            binding.onboardingScreensBodyEmphasized.text = emphasizedBodyText

            htmlTextPath = getString(R.styleable.OnboardingScreensView_bodyHtml) ?: ""
        }
    }

    fun getOnboardingHtmlText() {
        if (htmlTextPath.isNotEmpty()) {
            val bodyHtmlText = parseHtmlFromAssets(context, htmlTextPath)
            if (bodyHtmlText.isNotEmpty()) {
                binding.onboardingScreensBody.isGone = bodyHtmlText.isEmpty()
                binding.onboardingScreensBody.text = bodyHtmlText
                binding.onboardingScreensBody.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }
}
