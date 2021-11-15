package de.rki.coronawarnapp.qrcode.ui.info

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentUqsInfoBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class UqsInfoFragment : Fragment(R.layout.fragment_uqs_info) {

    private val binding: FragmentUqsInfoBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            privacyInformation.setOnClickListener {
                findNavController().navigate("coronawarnapp://contactdiary/dataPrivacy".toUri())
            }
        }
    }
}
