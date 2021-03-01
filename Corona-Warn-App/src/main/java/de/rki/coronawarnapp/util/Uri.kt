package de.rki.coronawarnapp.util

import android.net.Uri
import androidx.navigation.NavController
import java.util.Locale

/**
 * [NavController.navigate] by Uri is case sensitive. When authority and/or scheme are
 * in Uppercase letter an Exception will thrown.
 * To avoid such cases [navUri] is converting Uri schema and authority to lowercase always.
 */
val Uri.navUri: Uri
    get() = Uri.Builder()
        .authority(authority?.toLowerCase(Locale.ROOT))
        .scheme(scheme?.toLowerCase(Locale.ROOT))
        .path(path).build()
