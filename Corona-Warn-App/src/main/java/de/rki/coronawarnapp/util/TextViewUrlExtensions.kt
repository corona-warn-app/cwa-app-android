import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.annotation.StringRes
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.urlSpan
import timber.log.Timber

fun TextView.setTextWithUrl(@StringRes textRes: Int, @StringRes labelRes: Int, @StringRes urlRes: Int) {
    setTextWithUrls(textRes.toResolvingString(), TextViewUrlSet(labelRes, urlRes))
}

fun TextView.setTextWithUrl(@StringRes textRes: Int, label: String, url: String) {
    setTextWithUrls(textRes.toResolvingString(), TextViewUrlSet(label, url))
}

fun TextView.setTextWithUrl(content: String, label: String, url: String) {
    setTextWithUrls(content.toLazyString(), TextViewUrlSet(label, url))
}

fun TextView.setTextWithUrls(
    content: LazyString,
    vararg urls: TextViewUrlSet
) {
    if (urls.isEmpty()) {
        Timber.w("Urls text urls sets is empty!")
    }

    val contentResolved = content.get(context)
    val stringBuilder = SpannableStringBuilder(contentResolved)

    urls
        .map {
            it.label.get(context) to it.url.get(context)
        }
        .forEach { (label, url) ->
            val index = contentResolved.indexOf(label)
            if (index == -1) {
                Timber.w("Label $label was not found in $content")
                return@forEach
            }
            stringBuilder.urlSpan(index, index + label.length, url)
        }

    setText(
        stringBuilder,
        TextView.BufferType.SPANNABLE
    )
    movementMethod = LinkMovementMethod.getInstance()
}

data class TextViewUrlSet(
    val label: LazyString,
    val url: LazyString
) {
    constructor(
        @StringRes labelResource: Int,
        @StringRes urlResource: Int
    ) : this(label = labelResource.toResolvingString(), url = urlResource.toResolvingString())

    constructor(labelString: String, urlString: String) : this(
        label = labelString.toLazyString(),
        url = urlString.toLazyString()
    )
}
