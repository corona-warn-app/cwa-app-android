package de.rki.coronawarnapp.util

import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf

/**
 * Puts the given [pairs] as a [Bundle] into this [Intent].
 */
fun Intent.withExtras(vararg pairs: Pair<String, Any?>): Intent = putExtras(bundleOf(*pairs))
