package de.rki.coronawarnapp.statistics.ui

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentStatisticsExplanationBinding
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.ui.viewBindingLazy

/**
 * The fragment displays static informative content to the user
 * and represents one way to gain more detailed understanding of the
 * statistics and its trends.
 *
 */

class StatisticsExplanationFragment : Fragment(R.layout.fragment_statistics_explanation) {

    private val binding: FragmentStatisticsExplanationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        binding.statisticsExplanationSevenDayRValueText.setUrl(
            R.string.statistics_explanation_seven_day_r_value_text,
            R.string.statistics_explanation_seven_day_r_link_label,
            R.string.statistics_explanation_faq_url
        )
    }

    override fun onResume() {
        super.onResume()
        binding.statisticsExplanationContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.statisticsExplanationHeaderButtonBack.buttonIcon.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}
