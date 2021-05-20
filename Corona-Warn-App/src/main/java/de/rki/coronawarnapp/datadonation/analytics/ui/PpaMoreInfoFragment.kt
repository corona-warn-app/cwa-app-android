package de.rki.coronawarnapp.datadonation.analytics.ui

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentPpaMoreInfoBinding
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * The fragment displays static informative content to the user
 * and represents one way to gain more detailed understanding of the
 * data donation.
 *
 */

class PpaMoreInfoFragment : Fragment(R.layout.fragment_ppa_more_info) {

    private val binding: FragmentPpaMoreInfoBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.container.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.buttonBack.buttonIcon.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}
