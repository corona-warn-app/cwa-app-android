package de.rki.coronawarnapp.covidcertificate.validation.core.country

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class DccCountryLocalCacheTest : BaseIOTest() {

    private val cacheDir = File(IO_TEST_BASEDIR, "cache")

    private fun createInstance(): DccCountryLocalCache = DccCountryLocalCache(
        cacheDir = cacheDir
    )

    @Test
    fun `write and read data`() = runBlockingTest {

        createInstance().apply {
            loadJson() shouldBe null
            saveJson("{äöü")
        }

        createInstance().apply {
            loadJson() shouldBe "{äöü"
            saveJson(null)
            loadJson() shouldBe null
        }
    }
}
