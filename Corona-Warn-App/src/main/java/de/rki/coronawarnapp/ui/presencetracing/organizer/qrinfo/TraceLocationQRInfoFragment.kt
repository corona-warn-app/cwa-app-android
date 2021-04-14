package de.rki.coronawarnapp.ui.presencetracing.organizer.qrinfo

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerQrCodeInfoFragmentBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.TraceLocationOrganizerSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class TraceLocationQRInfoFragment : Fragment(R.layout.trace_location_organizer_qr_code_info_fragment), AutoInject {

    private val binding: TraceLocationOrganizerQrCodeInfoFragmentBinding by viewBindingLazy()

    @Inject lateinit var settings: TraceLocationOrganizerSettings

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

                    if (isAlreadyOnboarded()) {
                        popBackStack()
                    } else {
                        settings.qrInfoAcknowledged = true
                        doNavigate(
                            TraceLocationQRInfoFragmentDirections
                                .actionTraceLocationOrganizerQRInfoFragmentToTraceLocationOrganizerListFragment()
                        )
                    }
                }
            }
        }
    }

    private fun isAlreadyOnboarded(): Boolean {
        return settings.qrInfoAcknowledged
    }
}
