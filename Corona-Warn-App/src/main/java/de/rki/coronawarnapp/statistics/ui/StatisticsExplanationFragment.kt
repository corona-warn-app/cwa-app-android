package de.rki.coronawarnapp.statistics.ui

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentStatisticsExplanationBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import setTextWithUrl

/**
 * The fragment displays static informative content to the user
 * and represents one way to gain more detailed understanding of the
 * statistics and its trends.
 *
 */

class StatisticsExplanationFragment : Fragment(R.layout.fragment_statistics_explanation) {

    private val binding: FragmentStatisticsExplanationBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        binding.apply {
            statisticsFaq.setTextWithUrl(
                R.string.statistics_faq_text,
                R.string.statistics_faq_label,
                R.string.statistics_explanation_faq_url
            )

            statisticsExplanationTrendText.apply {
                val label = String.format(context.getString(R.string.statistics_explanation_trend_text))
                text = label
                contentDescription = label
            }

            blogLink.setTextWithUrl(
                R.string.statistics_explanation_blog,
                R.string.statistics_explanation_blog_label,
                R.string.statistics_explanation_blog_url
            )

            blogLink.setOnClickListener {
                openUrl(R.string.statistics_explanation_blog_url)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.statisticsExplanationContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }
}
