package de.rki.coronawarnapp.util.html

import android.content.res.AssetManager
import android.text.Spanned
import androidx.core.text.HtmlCompat
import dagger.Reusable
import de.rki.coronawarnapp.util.debug.measureTimeMillisWithResult
import timber.log.Timber
import javax.inject.Inject

@Reusable
class HtmlParser @Inject constructor(
    private val assets: AssetManager
) {

    fun parseByAssetPath(path: String): Spanned = assets.open(path).bufferedReader().use {
        Timber.v("parseByAssetPath($path)")
        parse(it.readText())
    }

    fun parse(html: String): Spanned {
        val (result, time) = measureTimeMillisWithResult {
            HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
        Timber.v("parse(html.length=${html.length}) took ${time}ms")
        return result
    }
}
