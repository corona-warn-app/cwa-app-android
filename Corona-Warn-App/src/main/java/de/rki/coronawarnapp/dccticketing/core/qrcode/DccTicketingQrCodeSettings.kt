package de.rki.coronawarnapp.dccticketing.core.qrcode

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.FlowPreference
import de.rki.coronawarnapp.util.preferences.clearAndNotify
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject

class DccTicketingQrCodeSettings @Inject constructor(
    @AppContext context: Context,
) {
    private val prefs by lazy {
        context.getSharedPreferences("dccTicketing_qrcode", Context.MODE_PRIVATE)
    }

    val checkServiceIdentity: FlowPreference<Boolean> = prefs.createFlowPreference(
        key = PREFS_KEY_CHECK_SERVICE_IDENTITY,
        defaultValue = true
    )

    fun clear() = prefs.clearAndNotify()
}

private const val PREFS_KEY_CHECK_SERVICE_IDENTITY = "dccTicketing_checkServiceIdentity"
