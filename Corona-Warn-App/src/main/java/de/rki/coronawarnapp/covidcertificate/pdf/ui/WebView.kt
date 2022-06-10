package de.rki.coronawarnapp.covidcertificate.pdf.ui

import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

fun WebView.setupWebView(onPageFinished: (view: WebView) -> Unit) {
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
                onPageFinished(view)
            }
        }
    }
}
