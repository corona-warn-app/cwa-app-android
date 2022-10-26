package de.rki.coronawarnapp.ccl.configuration.storage

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.BaseTest
import java.io.File

class DccBoosterRulesStorageTest : BaseTest() {

    private val cclFile = File(BaseIOTest.IO_TEST_BASEDIR, "permanent_file")
    private val cache = File(BaseIOTest.IO_TEST_BASEDIR, "cache_file")

    private fun createInstance() = DccBoosterRulesStorage(
        cclFile = cclFile,
        cacheDir = cache
    )

    @Test
    fun `write and read data with migration`() = runTest {
        val instance = createInstance()
        val cached = "cached booster rules"
        instance.boosterRulesCacheFile.apply {
            parentFile?.mkdirs()
            writeText(cached)
        }
        instance.loadBoosterRulesJson() shouldBe cached
        instance.boosterRulesCacheFile.exists() shouldBe true
        instance.boosterRulesFile.exists() shouldBe false
        val permanent = "permanent booster rules"
        instance.saveBoosterRulesJson(permanent)
        instance.boosterRulesCacheFile.exists() shouldBe false
        instance.boosterRulesFile.exists() shouldBe true
        instance.boosterRulesFile.readText() shouldBe permanent
        instance.loadBoosterRulesJson() shouldBe permanent
    }

    @AfterEach
    fun cleanup() {
        cclFile.deleteRecursively()
        cache.deleteRecursively()
    }
}
