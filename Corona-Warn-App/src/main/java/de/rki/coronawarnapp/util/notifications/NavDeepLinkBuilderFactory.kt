package de.rki.coronawarnapp.util.notifications

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.util.SafeNavDeepLinkBuilder
import javax.inject.Inject

@Reusable
class NavDeepLinkBuilderFactory @Inject constructor() {
    fun create(context: Context): SafeNavDeepLinkBuilder {
        return SafeNavDeepLinkBuilder(context)
    }
}
