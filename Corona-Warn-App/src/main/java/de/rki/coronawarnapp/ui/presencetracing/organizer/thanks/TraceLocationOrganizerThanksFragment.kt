package de.rki.coronawarnapp.ui.presencetracing.organizer.thanks

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerThanksFragmentBinding
import de.rki.coronawarnapp.util.ui.viewBinding

class TraceLocationOrganizerThanksFragment : Fragment(R.layout.trace_location_organizer_thanks_fragment) {

    private val binding: TraceLocationOrganizerThanksFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { leave() }
            finishButton.setOnClickListener { leave() }
        }
    }

    private fun leave() {
        TODO()
    }
}
