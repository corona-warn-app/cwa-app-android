package de.rki.coronawarnapp.statistics

import android.content.Context
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class StatisticsModuleTest : BaseIOTest() {
    @MockK lateinit var context: Context

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheFiles = File(testDir, "cache")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.cacheDir } returns cacheFiles

        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createModule() = StatisticsModule()

    @Test
    fun `sideeffect free instantiation`() {
        shouldNotThrowAny {
            createModule()
        }
    }
}
