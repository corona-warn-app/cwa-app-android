package de.rki.coronawarnapp.notification

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.MainActivity

class ExposureNotificationIntentService : IntentService("ExposureNotificationIntentService") {

    companion object {
        private val TAG: String? = ExposureNotificationIntentService::class.simpleName
    }

    override fun onHandleIntent(p0: Intent?) {
        Log.i(TAG, "onHandleIntent")
        fireNotification()
    }

    private fun fireNotification() {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val takeBreakIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                takeBreakIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val notificationChannelId = getString(R.string.notification_channel_id)
        val notificationId = getString(R.string.notification_id)

        val notificationBuilder =
            NotificationCompat.Builder(this, notificationChannelId).apply {
                setContentTitle(getString(R.string.notification_headline))
                setContentText(getString(R.string.notification_body))
                setSmallIcon(R.drawable.ic_splash_logo)
                priority = NotificationCompat.PRIORITY_HIGH
                setContentIntent(pendingIntent)
            }
        val notification = notificationBuilder.build()

        notificationManager.notify(notificationId.toInt(), notification)
    }
}
