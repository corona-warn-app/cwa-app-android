package de.rki.coronawarnapp.covidcertificate.pdf.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CertificatePdfExportInfoFragmentBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class CertificatePdfExportInfoFragment : Fragment(R.layout.certificate_pdf_export_info_fragment) {

    private val binding: CertificatePdfExportInfoFragmentBinding by viewBinding()
    private val args by navArgs<CertificatePdfExportInfoFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            nextButton.setOnClickListener {
                findNavController().navigate(
                    CertificatePdfExportInfoFragmentDirections
                        .actionCertificatePdfExportInfoFragmentToCertificatePosterFragment(args.containerId)
                )
            }
            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }
        }
    }
}
