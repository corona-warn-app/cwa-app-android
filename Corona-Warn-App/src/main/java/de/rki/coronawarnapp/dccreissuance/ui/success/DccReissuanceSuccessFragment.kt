package de.rki.coronawarnapp.dccreissuance.ui.success

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccReissuanceSuccessBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class DccReissuanceSuccessFragment : Fragment(R.layout.fragment_dcc_reissuance_success) {

    private val binding: FragmentDccReissuanceSuccessBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            dccReissuanceSuccessCompleteButton.setOnClickListener {
                popBackStack()
            }
        }
    }
}
