package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.tan

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerWarnTanFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.ui.addTitleId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted

class TraceLocationWarnTanFragment : Fragment(R.layout.trace_location_organizer_warn_tan_fragment), AutoInject {

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

        viewModel.state.observe(viewLifecycleOwner) {
            binding.apply {
                tanButtonEnter.isActive = it.isTanValid
                tanContent.submissionTanCharacterError.isGone = it.areCharactersCorrect
                if (it.isCorrectLength) {
                    tanContent.submissionTanError.isGone = it.isTanValid
                } else {
                    tanContent.submissionTanError.isGone = true
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
                tanButtonEnter.hideKeyboard()
                viewModel.startTanSubmission()
            }
            toolbar.setNavigationOnClickListener { goBack() }
            toolbar.addTitleId(R.id.trace_location_organizer_warn_tan_fragment_title_id)
        }

        viewModel.registrationState.observe(viewLifecycleOwner) {
            binding.tanButtonEnter.isLoading = it == ApiRequestState.STARTED
            if (ApiRequestState.SUCCESS == it) {
                findNavController().navigate(
                    TraceLocationWarnTanFragmentDirections
                        .actionTraceLocationTanDurationFragmentToTraceLocationOrganizerThanksFragment()
                )
            }
        }

        viewModel.registrationError.observe(viewLifecycleOwner) { displayDialog { setError(it) } }
    }

    override fun onResume() {
        super.onResume()
        binding.tanRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun goBack() {
        binding.root.hideKeyboard()
        popBackStack()
    }
}
