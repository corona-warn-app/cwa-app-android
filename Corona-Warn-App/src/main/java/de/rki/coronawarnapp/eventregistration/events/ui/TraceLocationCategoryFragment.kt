package de.rki.coronawarnapp.eventregistration.events.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.EventRegistrationCategoryFragmentBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class TraceLocationCategoryFragment : Fragment(R.layout.event_registration_category_fragment) {

    private val binding: EventRegistrationCategoryFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.categoryNavbar.headerButtonBack.buttonIcon.setOnClickListener {
            popBackStack()
        }
    }
}
