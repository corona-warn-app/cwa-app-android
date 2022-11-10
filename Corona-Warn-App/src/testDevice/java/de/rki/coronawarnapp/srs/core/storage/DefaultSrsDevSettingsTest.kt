package de.rki.coronawarnapp.srs.core.storage

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class DefaultSrsDevSettingsTest : BaseTest() {

    @Test
    fun getCheckLocalPrerequisites() = runTest {
        instance().checkLocalPrerequisites() shouldBe true
    }

    @Test
    fun getForceAndroidIdAcceptance() = runTest {
        instance().forceAndroidIdAcceptance() shouldBe false
    }

    @Test
    fun getDeviceTimeState() = runTest {
        instance().deviceTimeState() shouldBe null
    }

    private fun instance() = DefaultSrsDevSettings()
}
