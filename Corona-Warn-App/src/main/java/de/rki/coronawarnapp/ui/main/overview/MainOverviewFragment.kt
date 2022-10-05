package de.rki.coronawarnapp.ui.main.overview

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentMainOverviewBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The fragment displays static informative content to the user
 * and represents one way to gain more detailed understanding of the
 * app and its content.
 *
 */

class MainOverviewFragment : Fragment(R.layout.fragment_main_overview), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: MainOverviewViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentMainOverviewBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            viewModel.maxEncounterAgeInDays.observe2(this@MainOverviewFragment) {
                setExposureLoggingPeriod(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mainOverviewContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun FragmentMainOverviewBinding.setExposureLoggingPeriod(maxEncounterAgeInDays: Long) {
        mainOverviewRisk.mainOverviewSegmentBody.text =
            getString(R.string.main_overview_body_risk, maxEncounterAgeInDays)
    }
}
