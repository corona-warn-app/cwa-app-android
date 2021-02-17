package de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata

import de.rki.coronawarnapp.BuildConfig
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ClientVersionParserTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance() = ClientVersionParser()

    @Test
    fun `client version is properly parsed`() {
        val currentVersionName = BuildConfig.VERSION_NAME
        val currentVersionNumber = BuildConfig.VERSION_CODE

        val parsedVersion = createInstance().parseClientVersion(currentVersionNumber)

        parsedVersion.toVersionString() shouldBe currentVersionName
    }
}
