package de.rki.coronawarnapp.ui.settings.notification

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class NotificationSettingsStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `isNotificationsEnabled`() {
        TODO()
    }

    @Test
    fun `isNotificationsRiskEnabled`() {
        TODO()
    }

    @Test
    fun `isNotificationsTestEnabled`() {
        TODO()
    }

    @Test
    fun `isNotificationsEnabled and isNotificationsRiskEnabled`() {
        TODO()
    }

    @Test
    fun `isNotificationsEnabled and isNotificationsTestEnabled`() {
        TODO()
    }
}
