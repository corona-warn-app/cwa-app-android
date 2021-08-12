package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerWarnInfoFragmentBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.TraceLocationOrganizerSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class TraceLocationWarnInfoFragment : Fragment(R.layout.trace_location_organizer_warn_info_fragment), AutoInject {

    private val binding: TraceLocationOrganizerWarnInfoFragmentBinding by viewBinding()

    @Inject lateinit var settings: TraceLocationOrganizerSettings

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: TraceLocationWarnInfoViewModel by cwaViewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            nextButton.setOnClickListener { viewModel.onProceed() }
        }

        viewModel.proceed.observe2(this) {

            if (settings.warnInfoAcknowledged) {
                popBackStack()
            } else {
                settings.warnInfoAcknowledged = true
                // TODO: navigate here
            }
        }
    }
}
