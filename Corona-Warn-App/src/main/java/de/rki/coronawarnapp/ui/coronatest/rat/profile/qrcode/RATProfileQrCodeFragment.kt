package de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.RatProfileQrCodeFragmentBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class RATProfileQrCodeFragment : Fragment(R.layout.rat_profile_qr_code_fragment) {

    private val binding: RatProfileQrCodeFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.closeButton.setOnClickListener { popBackStack() }
    }
}
