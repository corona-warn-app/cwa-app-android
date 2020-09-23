package de.rki.coronawarnapp.environment

import de.rki.coronawarnapp.BuildConfig
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class EnvironmentSetupTest : BaseTest() {

    @Test
    fun `test mocking BuildConfig`() {
        mockkObject(BuildConfigWrap)
        BuildConfigWrap.DOWNLOAD_CDN_URL shouldBe BuildConfig.DOWNLOAD_CDN_URL

        every { BuildConfigWrap.DOWNLOAD_CDN_URL } returns "cake"
        BuildConfigWrap.DOWNLOAD_CDN_URL shouldBe "cake"
    }

    @Test
    fun `parsing bad json throws an exception in debug builds`() {
        TODO()
    }

    @Test
    fun `parsing bad json does not throw exception non-debug builds`() {
        TODO()
    }

    @Test
    fun `mapping between function ands JSON variable names is correct`() {
        TODO()
    }

    @Test
    fun `default environment type is set correctly`() {
        TODO()
    }

    @Test
    fun `switching the current environment type causes new urls to be returned`() {
        TODO()
    }

    @Test
    fun `switching the default type is persisted in storage (preferences)`() {
        TODO()
    }
}
