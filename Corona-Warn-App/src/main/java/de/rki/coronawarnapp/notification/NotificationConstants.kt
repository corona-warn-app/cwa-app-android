package de.rki.coronawarnapp.notification

import java.time.Duration

/**
 * The notification constants are used inside the NotificationHelper
 *
 * @see GeneralNotifications
 */
object NotificationConstants {

    const val NOTIFICATION_ID = "NOTIFICATION_ID"

    const val POSITIVE_RESULT_NOTIFICATION_TEST_TYPE = "NOTIFICATION_TEST_TYPE"
    const val POSITIVE_RESULT_NOTIFICATION_TEST_ID = "NOTIFICATION_TEST_ID"
    const val POSITIVE_LEGACY_RESULT_NOTIFICATION_ID = 100
    const val POSITIVE_PCR_RESULT_NOTIFICATION_ID = 101
    const val POSITIVE_RAT_RESULT_NOTIFICATION_ID = 102
    const val POSITIVE_RESULT_NOTIFICATION_TOTAL_COUNT = 2
    val POSITIVE_RESULT_NOTIFICATION_INITIAL_OFFSET: Duration = Duration.ofHours(2)
    val POSITIVE_RESULT_NOTIFICATION_INTERVAL: Duration = Duration.ofHours(2)

    const val DEADMAN_NOTIFICATION_ID: NotificationId = 3
    const val NEW_MESSAGE_RISK_LEVEL_SCORE_NOTIFICATION_ID: NotificationId = 110
    const val PCR_TEST_RESULT_AVAILABLE_NOTIFICATION_ID: NotificationId = 130
    const val INCORRECT_DEVICE_TIME_NOTIFICATION_ID: NotificationId = 140
    const val RAT_TEST_RESULT_AVAILABLE_NOTIFICATION_ID: NotificationId = 150
    const val TRACELOCATION_AUTOCHECKOUT_NOTIFICATION_ID: NotificationId = 1001
    const val DCC_STATE_CHECK_NOTIFICATION_ID: NotificationId = 2001
    const val FAMILY_TEST_RESULT_AVAILABLE_NOTIFICATION_ID: NotificationId = 2_220_3
}
