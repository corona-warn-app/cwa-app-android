package de.rki.coronawarnapp.util.gplay

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Reusable
class GoogleApiVersion @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val apiAvailability = GoogleApiAvailability.getInstance()

    fun isPlayServicesVersionAvailable(requiredVersion: Int): Boolean {
        return apiAvailability.isGooglePlayServicesAvailable(context, requiredVersion) == ConnectionResult.SUCCESS
    }
}
