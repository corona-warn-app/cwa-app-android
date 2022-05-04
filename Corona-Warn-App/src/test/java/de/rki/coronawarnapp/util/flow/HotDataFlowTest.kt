package de.rki.coronawarnapp.util.flow

import de.rki.coronawarnapp.util.mutate
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.coroutines.test
import java.io.IOException
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class HotDataFlowTest : BaseTest() {

    // Without an init value, there isn't a way to keep using the flow
    @Test
    fun `exceptions on initializen are rethrown`() {
        val testScope = TestScope()
        val hotData = HotDataFlow<String>(
            loggingTag = "tag",
            scope = testScope,
            coroutineContext = Dispatchers.Unconfined,
            startValueProvider = { throw IOException() }
        )
        runBlocking {
            withTimeoutOrNull(500) {
                // This blocking scope get's the init exception as the first caller
                hotData.data.firstOrNull()
            } shouldBe null
        }

        testScope.advanceUntilIdle()
    }

    @Test
    fun `subscription ends when no subscriber is collecting, mode WhileSubscribed`() {
        val testScope = TestScope()
        val valueProvider = mockk<suspend CoroutineScope.() -> String>()
        coEvery { valueProvider.invoke(any()) } returns "Test"

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            coroutineContext = Dispatchers.Unconfined,
            startValueProvider = valueProvider,
            sharingBehavior = SharingStarted.WhileSubscribed()
        )

        testScope.apply {
            runTest2(ignoreActive = true) {
                hotData.data.first() shouldBe "Test"
                hotData.data.first() shouldBe "Test"
            }
            coVerify(exactly = 2) { valueProvider.invoke(any()) }
        }
    }

    @Test
    fun `subscription doesn't end when no subscriber is collecting, mode Lazily`() {
        val testScope = TestScope()
        val valueProvider = mockk<suspend CoroutineScope.() -> String>()
        coEvery { valueProvider.invoke(any()) } returns "Test"

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            coroutineContext = Dispatchers.Unconfined,
            startValueProvider = valueProvider,
            sharingBehavior = SharingStarted.Lazily
        )

        testScope.apply {
            runTest2(ignoreActive = true) {
                hotData.data.first() shouldBe "Test"
                hotData.data.first() shouldBe "Test"
            }
            coVerify(exactly = 1) { valueProvider.invoke(any()) }
        }
    }

    @Test
    fun `value updates`() {
        val testScope = TestScope()
        val valueProvider = mockk<suspend CoroutineScope.() -> Long>()
        coEvery { valueProvider.invoke(any()) } returns 1

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            startValueProvider = valueProvider,
            sharingBehavior = SharingStarted.Lazily
        )

        val testCollector = hotData.data.test(startOnScope = testScope)
        testCollector.silent = true

        (1..16).forEach { _ ->
            thread {
                (1..200).forEach { _ ->
                    sleep(10)
                    hotData.updateAsync(
                        onUpdate = { this + 1L },
                        onError = { throw it }
                    )
                }
            }
        }

        runBlocking {
            testCollector.await { list, _ -> list.size == 3201 }
            testCollector.latestValues shouldBe (1..3201).toList()
        }

        coVerify(exactly = 1) { valueProvider.invoke(any()) }
    }

    data class TestData(
        val number: Long = 1
    )

    @Test
    fun `check multi threading value updates with more complex data`() {
        val testScope = TestScope()
        val valueProvider = mockk<suspend CoroutineScope.() -> Map<String, TestData>>()
        coEvery { valueProvider.invoke(any()) } returns mapOf("data" to TestData())

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            startValueProvider = valueProvider,
            sharingBehavior = SharingStarted.Lazily
        )

        val testCollector = hotData.data.test(startOnScope = testScope)
        testCollector.silent = true

        (1..10).forEach { _ ->
            thread {
                (1..400).forEach { _ ->
                    hotData.updateAsync {
                        mutate {
                            this["data"] = getValue("data").copy(
                                number = getValue("data").number + 1
                            )
                        }
                    }
                }
            }
        }

        runBlocking {
            testCollector.await { list, _ -> list.size == 4001 }
            testCollector.latestValues.map { it.values.single().number } shouldBe (1L..4001L).toList()
        }

        coVerify(exactly = 1) { valueProvider.invoke(any()) }
    }

    @Test
    fun `only emit new values if they actually changed updates`() {
        val testScope = TestScope()

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            startValueProvider = { "1" },
            sharingBehavior = SharingStarted.Lazily
        )

        val testCollector = hotData.data.test(startOnScope = testScope)
        testCollector.silent = true

        hotData.updateAsync { "1" }
        hotData.updateAsync { "2" }
        hotData.updateAsync { "2" }
        hotData.updateAsync { "1" }

        runBlocking {
            testCollector.await { list, _ -> list.size == 3 }
            testCollector.latestValues shouldBe listOf("1", "2", "1")
        }
    }

    @Test
    fun `multiple subscribers share the flow`() {
        val testScope = TestScope()
        val valueProvider = mockk<suspend CoroutineScope.() -> String>()
        coEvery { valueProvider.invoke(any()) } returns "Test"

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            startValueProvider = valueProvider,
            sharingBehavior = SharingStarted.Lazily
        )

        testScope.runTest2(ignoreActive = true) {
            val sub1 = hotData.data.test(tag = "sub1", startOnScope = this)
            val sub2 = hotData.data.test(tag = "sub2", startOnScope = this)
            val sub3 = hotData.data.test(tag = "sub3", startOnScope = this)

            hotData.updateAsync { "A" }
            hotData.updateAsync { "B" }
            hotData.updateAsync { "C" }

            listOf(sub1, sub2, sub3).forEach {
                it.await { list, _ -> list.size == 4 }
                it.latestValues shouldBe listOf("Test", "A", "B", "C")
                it.cancel()
            }

            hotData.data.first() shouldBe "C"
        }
        coVerify(exactly = 1) { valueProvider.invoke(any()) }
    }

    @Test
    fun `update queue is wiped on completion`() = runTest2(ignoreActive = true) {
        val valueProvider = mockk<suspend CoroutineScope.() -> Long>()
        coEvery { valueProvider.invoke(any()) } returns 1

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = this,
            coroutineContext = this.coroutineContext,
            startValueProvider = valueProvider,
            sharingBehavior = SharingStarted.WhileSubscribed(replayExpirationMillis = 0)
        )

        val testCollector1 = hotData.data.test(tag = "collector1", startOnScope = this)
        testCollector1.silent = false

        (1..10).forEach { _ ->
            hotData.updateAsync {
                this + 1L
            }
        }

        advanceUntilIdle()

        testCollector1.await { list, _ -> list.size == 11 }
        testCollector1.latestValues shouldBe (1L..11L).toList()

        testCollector1.cancel()
        testCollector1.awaitFinal()

        val testCollector2 = hotData.data.test(tag = "collector2", startOnScope = this)
        testCollector2.silent = false

        advanceUntilIdle()

        testCollector2.cancel()
        testCollector2.awaitFinal()

        testCollector2.latestValues shouldBe listOf(1L)

        coVerify(exactly = 2) { valueProvider.invoke(any()) }
    }

    @Test
    fun `blocking update is actually blocking`() = runBlocking {
        val testScope = TestScope()
        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            coroutineContext = testScope.coroutineContext,
            startValueProvider = {
                delay(2000)
                2
            },
            sharingBehavior = SharingStarted.Lazily
        )

        hotData.updateAsync {
            delay(2000)
            this + 1
        }

        val testCollector = hotData.data.test(startOnScope = testScope)

        testScope.advanceUntilIdle()

        hotData.updateBlocking { this - 3 } shouldBe 0

        testCollector.await { _, i -> i == 3 }
        testCollector.latestValues shouldBe listOf(2, 3, 0)

        testCollector.cancel()
    }

    @Test
    fun `blocking update rethrows error`() = runBlocking {
        val testScope = TestScope()
        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            coroutineContext = testScope.coroutineContext,
            startValueProvider = {
                delay(2000)
                2
            },
            sharingBehavior = SharingStarted.Lazily
        )

        val testCollector = hotData.data.test(startOnScope = testScope)

        testScope.advanceUntilIdle()

        shouldThrow<IOException> {
            hotData.updateBlocking { throw IOException("Suprise") } shouldBe 0
        }
        hotData.data.first() shouldBe 2

        hotData.updateBlocking { 3 } shouldBe 3
        hotData.data.first() shouldBe 3

        testCollector.cancel()
    }

    @Test
    fun `async updates error handler`() {
        val testScope = TestScope()

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            startValueProvider = { 1 },
            sharingBehavior = SharingStarted.Lazily
        )

        val testCollector = hotData.data.test(startOnScope = testScope)
        testScope.advanceUntilIdle()

        hotData.updateAsync { throw IOException("Suprise") }

        testScope.advanceUntilIdle()
        testCollector.cancel()
    }

    @Test
    fun `async updates rethrow errors on hotdata scope if no error handler is set`() = runBlocking {
        val testScope = TestScope()

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            startValueProvider = { 1 },
            sharingBehavior = SharingStarted.Lazily
        )

        val testCollector = hotData.data.test(startOnScope = testScope)
        testScope.advanceUntilIdle()

        var thrownError: Exception? = null

        hotData.updateAsync(
            onUpdate = { throw IOException("Suprise") },
            onError = { thrownError = it }
        )

        testScope.advanceUntilIdle()
        thrownError!!.shouldBeInstanceOf<IOException>()

        testCollector.cancel()
    }
}
