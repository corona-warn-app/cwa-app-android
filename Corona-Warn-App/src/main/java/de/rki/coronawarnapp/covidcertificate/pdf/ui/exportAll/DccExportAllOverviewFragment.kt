package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.os.Bundle
import android.print.PdfFile
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.getSystemService
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.databinding.FragmentDccExportAllOverviewBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.files.FileSharing
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import javax.inject.Inject

class DccExportAllOverviewFragment : Fragment(R.layout.fragment_dcc_export_all_overview), AutoInject {
    private val binding by viewBinding<FragmentDccExportAllOverviewBinding>()
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<DccExportAllOverviewViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        toolbar.setNavigationOnClickListener { popBackStack() }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_print -> webView.print()

                R.id.action_share -> webView.sharePDF()
            }

            true
        }
        viewModel.dccData.observe(viewLifecycleOwner) { data ->
            progressBar.isIndeterminate = false
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
                        progressLayout.isVisible = false
                        if (toolbar.menu.isEmpty()) {
                            toolbar.inflateMenu(R.menu.menu_certificate_poster)
                        }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        progressBar.progress = newProgress
                    }
                }
            }.loadDataWithBaseURL(
                null,
                data,
                "text/HTML",
                Charsets.UTF_8.name(),
                null
            )
        }
    }

    private fun WebView.print() {
        runCatching {
            val printManager = requireNotNull(requireContext().getSystemService<PrintManager>())
            val jobName = getString(R.string.app_name) + "-" + Instant.now().toString()
            val printAdapter = createPrintDocumentAdapter(jobName)
            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes
                    .Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .build()
            )
        }.onFailure { e ->
            e.toErrorDialogBuilder(requireContext()).show()
        }
    }

    private fun WebView.sharePDF() = lifecycleScope.launch {
        val jobName = getString(R.string.app_name) + " Document";
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build();
        val path = File(requireContext().cacheDir, "/export")
        val pdfPrint = PdfFile(attributes)
        pdfPrint.save(createPrintDocumentAdapter(jobName), path, "cet.pdf")
        FileSharing(requireContext()).getFileIntentProvider(
            File(requireContext().cacheDir, "/export/cet.pdf"),
            "ok",
            true
        )
            .intent(requireActivity()).also {
                startActivity(it)
            }
    }
}
