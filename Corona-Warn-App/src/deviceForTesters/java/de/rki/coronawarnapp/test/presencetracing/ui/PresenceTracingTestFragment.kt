package de.rki.coronawarnapp.test.presencetracing.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannedString
import android.view.View
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestPresenceTracingBinding
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class PresenceTracingTestFragment : Fragment(R.layout.fragment_test_presence_tracing), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: PresenceTracingTestViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentTestPresenceTracingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        binding.openConsent.setOnClickListener {
            findNavController().navigate(R.id.checkInsConsentFragment)
        }

        viewModel.lastOrganiserLocation.observe(viewLifecycleOwner) {
            binding.lastOrganiserLocationCard.isVisible = it != null
            it?.let { traceLocation ->
                with(binding) {
                    lastOrganiserLocation.text = traceLocationText(traceLocation)
                    lastOrganiserLocationId.text = styleText("ID", traceLocation.locationId.base64())
                    lastOrganiserLocationUrl.text = styleText("URL", traceLocation.locationUrl)
                    qrcodeButton.setOnClickListener {
                        findNavController().navigate(
                            PresenceTracingTestFragmentDirections
                                .actionPresenceTracingTestFragmentToQrCodePosterTestFragment(traceLocation.id)
                        )
                    }

                    submitButton.setOnClickListener {
                        viewModel.submit(
                            traceLocation,
                            organizerTan.text.toString(),
                            organizerStartDate.text.toString(),
                            organizerDuration.text.toString()
                        )
                    }
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { displayDialog { setError(it) } }

        viewModel.submissionResult.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Organizer submission passed", Toast.LENGTH_LONG).show()
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
                color(requireContext().getColorCompat(R.color.colorPrimary)) {
                    append("$key = ")
                }
            }

            scale(0.85f) {
                color(requireContext().getColorCompat(R.color.colorOnPrimary)) {
                    append(value.toString())
                }
            }
            appendLine()
        }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Presence Tracing",
            description = "View & Control presence tracing",
            targetId = R.id.presenceTracingTestFragment
        )
    }
}
