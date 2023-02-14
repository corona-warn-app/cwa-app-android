package de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerQrCodeInfoFragmentBinding
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class TraceLocationQRInfoFragment : Fragment(R.layout.trace_location_organizer_qr_code_info_fragment) {

    private val binding: TraceLocationOrganizerQrCodeInfoFragmentBinding by viewBinding()
    private val vm: TraceLocationQRInfoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            privacyInformation.setOnClickListener { vm.openPrivacyCard() }
            traceLocationQrInfoUnderstandButton.setOnClickListener { vm.navigateToMyQRCodes() }
        }

        vm.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                is TraceLocationQRInfoNavigationEvents.NavigateToDataPrivacy ->
                    findNavController().navigate("coronawarnapp://contactdiary/dataPrivacy".toUri())

                is TraceLocationQRInfoNavigationEvents.NavigateToMyQrCodes -> {

                    if (vm.isAlreadyOnboarded.value == true) {
                        popBackStack()
                    } else {
                        vm.updateQrInfoAcknowledged(true)
                        findNavController().navigate(
                            TraceLocationQRInfoFragmentDirections
                                .actionTraceLocationOrganizerQRInfoFragmentToTraceLocationOrganizerListFragment()
                        )
                    }
                }
            }
        }
    }
}
