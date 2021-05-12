package de.rki.coronawarnapp.environment

import de.rki.coronawarnapp.BuildConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BuildConfigWrapTest : BaseTest() {

    @Test
    fun `default environment type `() {
        if (BuildConfig.FLAVOR == "deviceForTesters" && BuildConfig.BUILD_TYPE == "debug") {
            BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT shouldBe "INT"
        } else if (BuildConfig.FLAVOR == "deviceForTesters" && BuildConfig.BUILD_TYPE == "release") {
            BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT shouldBe "WRU"
        } else if (BuildConfig.FLAVOR == "device" && BuildConfig.BUILD_TYPE == "debug") {
            BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT shouldBe "INT"
        } else if (BuildConfig.FLAVOR == "device" && BuildConfig.BUILD_TYPE == "release") {
            BuildConfigWrap.ENVIRONMENT_TYPE_DEFAULT shouldBe "PROD"
        } else {
            throw IllegalStateException("Unknown flavor/build combination.")
        }
    }
}
