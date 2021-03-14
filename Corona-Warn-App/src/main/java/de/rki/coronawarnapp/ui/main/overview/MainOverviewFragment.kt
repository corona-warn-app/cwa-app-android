package de.rki.coronawarnapp.ui.main.overview

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentMainOverviewBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ui.viewBindingLazy

/**
 * The fragment displays static informative content to the user
 * and represents one way to gain more detailed understanding of the
 * app and its content.
 *
 */

class MainOverviewFragment : Fragment(R.layout.fragment_main_overview) {

    private val binding: FragmentMainOverviewBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.mainOverviewContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.mainOverviewHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
