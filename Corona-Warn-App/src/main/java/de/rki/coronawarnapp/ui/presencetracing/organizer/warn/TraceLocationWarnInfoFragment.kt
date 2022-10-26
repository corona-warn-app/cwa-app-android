package de.rki.coronawarnapp.ui.presencetracing.organizer.warn

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerWarnInfoFragmentBinding
import de.rki.coronawarnapp.util.ui.addTitleId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class TraceLocationWarnInfoFragment : Fragment(R.layout.trace_location_organizer_warn_info_fragment) {

    private val binding: TraceLocationOrganizerWarnInfoFragmentBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.addTitleId(R.id.trace_location_organizer_warn_info_fragment_title_id)
            nextButton.setOnClickListener {
                findNavController().navigate(
                    TraceLocationWarnInfoFragmentDirections
                        .actionTraceLocationWarnInfoFragmentToTraceLocationSelectionFragment()
                )
            }
        }
    }
}
