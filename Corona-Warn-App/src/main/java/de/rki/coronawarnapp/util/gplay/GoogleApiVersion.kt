package de.rki.coronawarnapp.util.gplay

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.Reusable
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Reusable
class GoogleApiVersion @Inject constructor(
    @AppContext private val context: Context
) {

    private val apiAvailability = GoogleApiAvailability.getInstance()

    fun isPlayServicesVersionAvailable(requiredVersion: Int): Boolean {
        return apiAvailability.isGooglePlayServicesAvailable(context, requiredVersion) == ConnectionResult.SUCCESS
    }
}
