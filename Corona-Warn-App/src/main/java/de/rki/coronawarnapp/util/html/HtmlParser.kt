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

    fun parseByAssetPath(path: String): String = assets.open(path).bufferedReader()
        .use {
            Timber.v("parseByAssetPath($path)")
            it.readText()
        }
}
