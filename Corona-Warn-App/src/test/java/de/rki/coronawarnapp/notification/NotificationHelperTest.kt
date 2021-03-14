package de.rki.coronawarnapp.notification

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class NotificationHelperTest : BaseTest() {

    @Test
    fun `notification channel ID should never change`() {
        NotificationHelper.MAIN_CHANNEL_ID shouldBe "de.rki.coronawarnapp.notification.exposureNotificationChannelId"
    }
}
