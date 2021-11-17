package de.rki.coronawarnapp.qrcode.ui.info

import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentUqsInfoBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class UqsInfoFragment : Fragment(R.layout.fragment_uqs_info) {

    private val binding: FragmentUqsInfoBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            privacyInformation.setOnClickListener {
                findNavController().navigate(R.id.informationPrivacyFragment)
            }
            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }
        }
    }
}
