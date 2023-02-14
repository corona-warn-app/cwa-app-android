package de.rki.coronawarnapp.presencetracing.checkins.checkout.auto

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Reusable
class AutoCheckOutIntentFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createIntent(checkInId: Long? = null): PendingIntent {
        val updateServiceIntent = Intent(context, AutoCheckOutReceiver::class.java).apply {
            action = AutoCheckOutReceiver.ACTION_AUTO_CHECKOUT
            if (checkInId != null) {
                putExtra(AutoCheckOutReceiver.ARGKEY_RECEIVER_CHECKIN_ID, checkInId)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            updateServiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val REQUEST_CODE = 5410 // Ticket number ¯\_(ツ)_/¯
    }
}
