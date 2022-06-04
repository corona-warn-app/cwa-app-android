package de.rki.coronawarnapp.covidcertificate.pdf.ui.exportAll

import android.graphics.Bitmap
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.fragment.app.Fragment
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccExportAllOverviewBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DccExportAllOverviewFragment : Fragment(R.layout.fragment_dcc_export_all_overview), AutoInject {
    private val binding by viewBinding<FragmentDccExportAllOverviewBinding>()
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<DccExportAllOverviewViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        toolbar.setNavigationOnClickListener { popBackStack() }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_print -> printTheWebPage(webView)

                R.id.action_share -> {
                }
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

    private fun printTheWebPage(webView: WebView) {
        val printManager = requireContext().getSystemService<PrintManager>()!!
        val printAdapter = webView.createPrintDocumentAdapter(getString(R.string.app_name))
        printManager.print(
            getString(R.string.app_name),
            printAdapter,
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .build()
        )
    }
}
