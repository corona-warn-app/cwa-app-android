package de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage

import io.kotest.matchers.file.shouldBeEmpty
import io.kotest.matchers.file.shouldNotBeEmptyDirectory
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class DccTicketingAllowListStorageTest : BaseIOTest() {

    private val localStorage = File(IO_TEST_BASEDIR, DccTicketingAllowListStorageTest::class.java.simpleName)

    private val instance: DccTicketingAllowListStorage
        get() = DccTicketingAllowListStorage(localStorage = localStorage)

    private val data = "data".toByteArray()

    @AfterEach
    fun teardown() {
        localStorage.deleteRecursively()
    }

    @Test
    fun `returns null if no data exists`() = runTest {
        with(instance) {
            load() shouldBe null

            save(data)
            reset()
            load() shouldBe null
        }
    }

    @Test
    fun `returns saved data and override existing data`() = runTest {
        val data2 = "data2".toByteArray()

        with(instance) {
            save(data = data)
            load() shouldBe data

            save(data = data2)
            load() shouldNotBe data
            load() shouldBe data2

            save(data = data)
            load() shouldBe data
        }
    }

    @Test
    fun `clear erases all data`() = runBlocking {
        with(instance) {
            localStorage.shouldBeEmpty()
            save(data = data)
            localStorage.shouldNotBeEmptyDirectory()
            reset()
            localStorage.shouldBeEmpty()
        }
    }
}
