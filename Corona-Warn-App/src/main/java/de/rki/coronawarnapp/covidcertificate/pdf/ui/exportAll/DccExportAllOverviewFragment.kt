package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.DccExportAllOverviewViewModel.ExportResult
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.DccExportAllOverviewViewModel.PDFResult
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.DccExportAllOverviewViewModel.PrintResult
import de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll.DccExportAllOverviewViewModel.ShareResult
import de.rki.coronawarnapp.databinding.FragmentDccExportAllOverviewBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import java.time.Instant
import javax.inject.Inject

class DccExportAllOverviewFragment : Fragment(R.layout.fragment_dcc_export_all_overview), AutoInject {
    private val binding by viewBinding<FragmentDccExportAllOverviewBinding>()
    private val jobName get() = getString(R.string.app_name) + "-" + Instant.now().toString()

    @Inject
    lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<DccExportAllOverviewViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        setupToolbar()
        setupWebView()
        cancelButton.setOnClickListener { popBackStack() }
        with(viewModel) {
            error.observe(viewLifecycleOwner) { showErrorDialog() }
            exportResult.observe(viewLifecycleOwner) { handleExportResult(it) }
            pdfString.observe(viewLifecycleOwner) { loadData(it) }
        }
    }

    private fun FragmentDccExportAllOverviewBinding.loadData(
        data: String
    ) {
        webView.loadDataWithBaseURL(
            null,
            data,
            "text/HTML",
            Charsets.UTF_8.name(),
            null
        )
    }

    private fun FragmentDccExportAllOverviewBinding.setupToolbar() {
        toolbar.setNavigationOnClickListener { popBackStack() }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_print -> viewModel.print(webView.createPrintDocumentAdapter(jobName))
                R.id.action_share -> viewModel.sharePDF()
            }
            true
        }
    }

    private fun FragmentDccExportAllOverviewBinding.handleExportResult(
        exportResult: ExportResult
    ) {
        when (exportResult) {
            is ShareResult -> exportResult.provider.intent(requireActivity()).also { startActivity(it) }
            is PrintResult -> exportResult.print(requireActivity())
            is PDFResult -> {
                progressLayout.isVisible = false
                if (toolbar.menu.isEmpty()) {
                    toolbar.inflateMenu(R.menu.menu_certificate_poster)
                }
            }
        }
    }

    private fun showErrorDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.export_all_error_title)
            .setMessage(R.string.export_all_error_message)
            .setNeutralButton(R.string.export_all_error_faq) { _, _ ->
                openUrl(R.string.certificate_export_error_dialog_faq_link)
            }.setPositiveButton(android.R.string.ok) { _, _ -> }
            .setOnDismissListener { popBackStack() }
            .show()
    }

    private fun FragmentDccExportAllOverviewBinding.setupWebView() {
        webView.apply {
            with(settings) {
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                displayZoomControls = false
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    view?.let {
                        viewModel.createPDF(view.createPrintDocumentAdapter(jobName))
                    }
                }
            }
        }
    }
}
