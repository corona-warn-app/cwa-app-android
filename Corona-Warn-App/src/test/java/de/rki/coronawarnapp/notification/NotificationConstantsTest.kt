package de.rki.coronawarnapp.notification

import de.rki.coronawarnapp.R
import org.junit.Assert
import org.junit.Test

class NotificationConstantsTest {

    @Test
    fun allNotificationConstants() {
        Assert.assertEquals(NotificationConstants.NOTIFICATION_CHANNEL_ID, R.string.notification_channel_id)
        Assert.assertEquals(NotificationConstants.NOTIFICATION_SMALL_ICON, R.drawable.ic_splash_logo)
        Assert.assertEquals(NotificationConstants.CHANNEL_NAME, R.string.notification_name)
        Assert.assertEquals(NotificationConstants.CHANNEL_DESCRIPTION, R.string.notification_description)
        Assert.assertEquals(
            NotificationConstants.NOTIFICATION_CONTENT_TITLE_RISK_CHANGED,
            R.string.notification_headline
        )
        Assert.assertEquals(NotificationConstants.NOTIFICATION_CONTENT_TEXT_RISK_CHANGED, R.string.notification_body)
    }
}
