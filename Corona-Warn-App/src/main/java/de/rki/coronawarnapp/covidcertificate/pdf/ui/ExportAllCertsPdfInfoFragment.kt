package de.rki.coronawarnapp.covidcertificate.pdf.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ExportAllCertsPdfInfoFragmentBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class ExportAllCertsPdfInfoFragment : Fragment(R.layout.export_all_certs_pdf_info_fragment) {

    private val binding: ExportAllCertsPdfInfoFragmentBinding by viewBinding()

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
                    ExportAllCertsPdfInfoFragmentDirections
                        .actionExportAllCertsPdfInfoFragmentToDccExportAllOverviewFragment()
                )
            }
            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }
        }
    }
}
