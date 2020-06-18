package de.rki.coronawarnapp.ui.information

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationPrivacyBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import kotlinx.coroutines.channels.ActorScope
import java.io.InputStreamReader
import java.util.*

/**
 * Basic Fragment which only displays static content.
 */
class InformationPrivacyFragment : Fragment() {
    companion object {
        private val TAG: String? = InformationPrivacyFragment::class.simpleName
    }

    private var _binding: FragmentInformationPrivacyBinding? = null
    private val binding: FragmentInformationPrivacyBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationPrivacyBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.informationPrivacyContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.informationPrivacyHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        binding.savePrivacyConsentButton.setOnClickListener {

            var mWebView: WebView? = null
            val webView = WebView(activity)

            webView.webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

                override fun onPageFinished(view: WebView, url: String) {
                    createWebPrintJob(view)
                    mWebView = null
                }
            }

            if(Locale.getDefault().language == "de"){
                webView.loadUrl("file:///android_asset/privacy_de.html")
            }else{
                webView.loadUrl("file:///android_asset/privacy_en.html")
            }




            mWebView = webView

        }



    }

    private fun createWebPrintJob(webView: WebView) {

        // Get a PrintManager instance
        (activity?.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->

            val jobName = "${getString(R.string.app_name)} Document"

            // Get a print adapter instance
            val printAdapter = webView.createPrintDocumentAdapter(jobName)

            // Create a print job with name and adapter instance
            printManager.print(
                jobName,
                printAdapter,
                PrintAttributes.Builder().build()
            ).also { printJob ->

                // Save the job object for later status checking
                var printJobs = printJob
            }
        }
    }


}
