package de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerQrCodeInfoFragmentBinding
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class TraceLocationQRInfoFragment : Fragment(R.layout.trace_location_organizer_qr_code_info_fragment), AutoInject {

    private val binding: TraceLocationOrganizerQrCodeInfoFragmentBinding by viewBinding()

    @Inject lateinit var settings: TraceLocationPreferences

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TraceLocationQRInfoViewModel by cwaViewModels { viewModelFactory }

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

        vm.routeToScreen.observe2(this) {
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
