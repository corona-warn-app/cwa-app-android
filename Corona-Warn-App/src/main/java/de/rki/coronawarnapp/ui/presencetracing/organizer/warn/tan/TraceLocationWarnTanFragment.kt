package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.tan

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerWarnTanFragmentBinding
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class TraceLocationWarnTanFragment : Fragment(R.layout.trace_location_organizer_warn_tan_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val navArgs by navArgs<TraceLocationWarnTanFragmentArgs>()
    private val viewModel: TraceLocationWarnTanViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as TraceLocationWarnTanViewModel.Factory
            factory.create(navArgs.traceLocationWarnDuration)
        }
    )

    private val binding: TraceLocationOrganizerWarnTanFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe2(this) {
            binding.apply {
                uiState = it
                tanContent.submissionTanCharacterError.setGone(it.areCharactersCorrect)
                if (it.isCorrectLength) {
                    tanContent.submissionTanError.setGone(it.isTanValid)
                } else {
                    tanContent.submissionTanError.setGone(true)
                }
            }
        }

        binding.apply {
            tanContent.submissionTanInput.listener = { tan ->
                tanContent.submissionTanCharacterError.visibility = View.GONE
                tanContent.submissionTanError.visibility = View.GONE

                viewModel.onTanChanged(tan)
            }

            tanButtonEnter.setOnClickListener {
                viewModel.startTanSubmission()
            }
            tanHeader.headerButtonBack.buttonIcon.setOnClickListener { goBack() }
        }

        viewModel.registrationState.observe2(this) {
            binding.tanSpinner.visibility = when (it) {
                ApiRequestState.STARTED -> View.VISIBLE
                else -> View.GONE
            }

            if (ApiRequestState.SUCCESS == it) {
                doNavigate(
                    TraceLocationWarnTanFragmentDirections
                        .actionTraceLocationTanDurationFragmentToTraceLocationOrganizerThanksFragment()
                )
            }
        }

        viewModel.registrationError.observe2(this) {
            val errorDialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.trace_location_organiser_warn_tan_error_header,
                it.errorMessage.get(requireContext()),
                R.string.trace_location_organiser_warn_tan_error_ok_button,
                null,
                true,
                ::goBack
            )
            DialogHelper.showDialog(errorDialog)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun goBack() {
        popBackStack()
    }
}
