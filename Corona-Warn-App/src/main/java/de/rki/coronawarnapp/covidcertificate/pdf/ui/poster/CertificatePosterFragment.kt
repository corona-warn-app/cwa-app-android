package de.rki.coronawarnapp.covidcertificate.pdf.ui.poster

import android.os.Bundle
import android.view.View
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.pdf.ui.poster.CertificatePosterViewModel.UiState.Done
import de.rki.coronawarnapp.covidcertificate.pdf.ui.poster.CertificatePosterViewModel.UiState.PDF
import de.rki.coronawarnapp.covidcertificate.pdf.ui.poster.CertificatePosterViewModel.UiState.PrintResult
import de.rki.coronawarnapp.covidcertificate.pdf.ui.setupWebView
import de.rki.coronawarnapp.databinding.CertificatePosterFragmentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import java.time.Instant
import javax.inject.Inject

class CertificatePosterFragment : Fragment(R.layout.certificate_poster_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<CertificatePosterFragmentArgs>()
    private val viewModel: CertificatePosterViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as CertificatePosterViewModel.Factory
            factory.create(
                containerId = args.containerId
            )
        }
    )

    private val binding: CertificatePosterFragmentBinding by viewBinding()
    private val jobName = "CoronaWarn-" + Instant.now().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_print -> viewModel.print(webView.createPrintDocumentAdapter(jobName))
                    R.id.action_share -> viewModel.sharePDF()
                }
                true
            }

            webView.setupWebView { view ->
                viewModel.createPDF(view.createPrintDocumentAdapter(jobName))
            }
        }

        viewModel.sharingIntent.observe(viewLifecycleOwner) { provider ->
            provider.intent(requireActivity()).also { startActivity(it) }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            binding.progressLayout.isVisible = false
            displayDialog { setError(it) }
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Done -> {
                    binding.progressLayout.isVisible = false
                    if (binding.toolbar.menu.isEmpty()) {
                        binding.toolbar.inflateMenu(R.menu.menu_certificate_poster)
                    }
                }

                is PDF -> binding.webView.loadDataWithBaseURL(
                    null,
                    state.pdfString,
                    "text/HTML",
                    "UTF-8",
                    null
                )

                is PrintResult -> state.print(requireActivity())
            }
        }
    }
}
