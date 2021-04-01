package de.rki.coronawarnapp.util.notifications

import androidx.core.app.NotificationCompat

fun NotificationCompat.Builder.setContentTextExpandable(
    contentText: String
) = apply {
    setContentText(contentText)
    setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
}
