package de.rki.coronawarnapp.util.notifications

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import dagger.Reusable
import javax.inject.Inject

@Reusable
class NavDeepLinkBuilderFactory @Inject constructor() {
    fun create(context: Context): NavDeepLinkBuilder {
        return NavDeepLinkBuilder(context)
    }
}
