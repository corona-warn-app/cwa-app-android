package de.rki.coronawarnapp.test.eventregistration.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannedString
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestEventregistrationBinding
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class EventRegistrationTestFragment : Fragment(R.layout.fragment_test_eventregistration), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: EventRegistrationTestFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestEventregistrationBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            scanCheckInQrCode.setOnClickListener {
                doNavigate(
                    EventRegistrationTestFragmentDirections
                        .actionEventRegistrationTestFragmentToScanCheckInQrCodeFragment()
                )
            }

            testQrCodeCreation.setOnClickListener {
                doNavigate(
                    EventRegistrationTestFragmentDirections
                        .actionEventRegistrationTestFragmentToTestQrCodeCreationFragment()
                )
            }

            createEventButton.setOnClickListener {
                findNavController().navigate(R.id.createEventTestFragment)
            }

            showEventsButton.setOnClickListener {
                findNavController().navigate(R.id.showStoredEventsTestFragment)
            }
        }

        binding.runMatcher.setOnClickListener {
            viewModel.runMatcher()
        }

        binding.resetProcessedWarningPackages.setOnClickListener {
            viewModel.resetProcessedWarningPackages()
        }

        binding.calculateRisk.setOnClickListener {
            viewModel.runRiskCalculationPerCheckInDay()
        }

        viewModel.presenceTracingWarningTaskResult.observe2(this) {
            binding.tracingWarningTaskResult.text = it
        }

        viewModel.checkInRiskPerDayText.observe2(this) {
            binding.riskCalculationResultText.text = it
        }

        viewModel.taskRunTime.observe2(this) {
            binding.taskRunTime.text = "Task finished in ${it}ms"
        }

        viewModel.riskCalculationRuntime.observe2(this) {
            binding.riskCalculationRuntimeText.text = "Risk calculation runtime in millis: $it"
        }

        binding.runPtWarningTask.setOnClickListener {
            viewModel.runPresenceTracingWarningTask()
        }

        viewModel.lastOrganiserLocation.observe(viewLifecycleOwner) {
            binding.lastOrganiserLocationCard.isVisible = it != null
            it?.let { traceLocation ->
                with(binding) {
                    lastOrganiserLocation.text = traceLocationText(traceLocation)
                    lastOrganiserLocationId.text = styleText("ID", traceLocation.locationId.base64())
                    lastOrganiserLocationUrl.text = styleText("URL", traceLocation.locationUrl)
                    qrcodeButton.setOnClickListener {
                        doNavigate(
                            EventRegistrationTestFragmentDirections
                                .actionEventRegistrationTestFragmentToQrCodePosterFragmentTest(traceLocation.id)
                        )
                    }
                }
            }
        }

        viewModel.lastAttendeeLocation.observe(viewLifecycleOwner) {
            binding.lastAttendeeLocationCard.isVisible = it != null
            it?.let { traceLocation ->
                with(binding) {
                    lastAttendeeLocation.text = traceLocationText(traceLocation)
                    lastAttendeeLocationId.text = styleText("ID", traceLocation.locationId.base64())
                    lastAttendeeLocationUrl.text = styleText("URL", traceLocation.locationUrl)
                }
            }
        }
    }

    private fun traceLocationText(traceLocation: TraceLocation): SpannedString = with(traceLocation) {
        buildSpannedString {
            append("TraceLocation [\n")
            append(styleText("Id", id))
            append(styleText("type", type))
            append(styleText("version", version))
            append(styleText("address", address))
            append(styleText("description", description))
            append(styleText("startDate", startDate))
            append(styleText("endDate", endDate))
            append(styleText("defaultCheckInLengthInMinutes", defaultCheckInLengthInMinutes))
            append(styleText("cnPublicKey", cnPublicKey))
            append(styleText("cryptographicSeed", cryptographicSeed.base64()))
            append("]")
        }
    }

    private fun styleText(key: String, value: Any?): SpannedString =
        buildSpannedString {
            bold {
                color(requireContext().getColorCompat(R.color.colorAccent)) {
                    append("$key=")
                }
            }

            scale(0.85f) {
                color(requireContext().getColorCompat(R.color.colorTextPrimary1)) {
                    append(value.toString())
                }
            }
            append("\n")
        }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Event Registration",
            description = "View & Control the event registration.",
            targetId = R.id.eventRegistrationTestFragment
        )
    }
}
