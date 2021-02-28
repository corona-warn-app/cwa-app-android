package de.rki.coronawarnapp.ui.eventregistration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentConfrimCheckInBinding
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class ConfirmCheckInFragment : Fragment(R.layout.fragment_confrim_check_in) {

    private val binding: FragmentConfrimCheckInBinding by viewBindingLazy()
    private val args by navArgs<ConfirmCheckInFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO implement Event verification and verify check in
        binding.encodedEvent.text = "Encoded event:${args.event}"
    }
}
