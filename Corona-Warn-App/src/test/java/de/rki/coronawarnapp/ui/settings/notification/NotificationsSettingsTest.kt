package de.rki.coronawarnapp.ui.settings.notification

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class NotificationsSettingsTest : BaseTest() {

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
    fun `poll global notification status when foregroundstate changes`() {
        TODO()
    }

    @Test
    fun `toggle risk notifications`() {
        TODO()
    }

    @Test
    fun `toggle test notifications`() {
        TODO()
    }
}
