package de.rki.coronawarnapp.environment

import de.rki.coronawarnapp.BuildConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BuildConfigWrapTest : BaseTest() {

    @Test
    fun `default environment type should be DEV`() {
        BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT shouldBe BuildConfig.ENVIRONMENT_TYPE_DEFAULT
    }

    @Test
    fun `alternative environment type should be WRU-XD`() {
        BuildConfigWrap.ENVIRONMENT_TYPE_ALTERNATIVE shouldBe BuildConfig.ENVIRONMENT_TYPE_ALTERNATIVE
    }
}
