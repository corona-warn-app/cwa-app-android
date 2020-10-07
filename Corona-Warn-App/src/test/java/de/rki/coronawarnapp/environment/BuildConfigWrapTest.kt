package de.rki.coronawarnapp.environment

import io.kotest.matchers.collections.shouldBeIn
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BuildConfigWrapTest : BaseTest() {

    @Test
    fun `default environment type should be DEV`() {
        BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT shouldBeIn listOf("DEV", "INT", "WRU-XD", "PROD")
    }
}
