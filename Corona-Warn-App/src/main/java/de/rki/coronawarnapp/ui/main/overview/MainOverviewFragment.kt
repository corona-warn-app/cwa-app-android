package de.rki.coronawarnapp.ui.main.overview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentMainOverviewBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * The fragment displays static informative content to the user
 * and represents one way to gain more detailed understanding of the
 * app and its content.
 *
 */

@AndroidEntryPoint
class MainOverviewFragment : Fragment(R.layout.fragment_main_overview) {

    private val viewModel: MainOverviewViewModel by viewModels()
    private val binding: FragmentMainOverviewBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            viewModel.maxEncounterAgeInDays.observe(viewLifecycleOwner) { setExposureLoggingPeriod(it) }
        }
    }

    private fun FragmentMainOverviewBinding.setExposureLoggingPeriod(maxEncounterAgeInDays: Long) {
        mainOverviewRisk.setSubtitleText(getString(R.string.main_overview_body_risk, maxEncounterAgeInDays))
    }
}
