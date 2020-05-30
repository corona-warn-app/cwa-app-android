package de.rki.coronawarnapp.util

import android.content.Intent
import android.net.Uri
import de.rki.coronawarnapp.ui.BaseFragment

object OpenUrlHelper {
    fun navigate(fragment: BaseFragment, url: String) {
        fragment.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }
}
