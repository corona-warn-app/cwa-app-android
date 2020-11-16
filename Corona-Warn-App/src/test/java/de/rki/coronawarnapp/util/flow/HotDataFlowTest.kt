package de.rki.coronawarnapp.util.flow

import de.rki.coronawarnapp.util.mutate
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.instanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.coroutines.test
import java.io.IOException
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class HotDataFlowTest : BaseTest() {

    @Test
    fun `init call only happens on first collection`() {
        val testScope = TestCoroutineScope()
        val hotData = HotDataFlow<String>(
            loggingTag = "tag",
            scope = testScope,
            coroutineContext = Dispatchers.Unconfined,
            startValueProvider = {
                throw IOException()
            }
        )

        testScope.apply {
            runBlockingTest2(permanentJobs = true) {
                hotData.data.first()
            }
            uncaughtExceptions.single() shouldBe instanceOf(IOException::class)
        }
    }

    @Test
    fun `subscription ends when no subscriber is collecting, mode WhileSubscribed`() {
        val testScope = TestCoroutineScope()
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
            runBlockingTest2(permanentJobs = true) {
                hotData.data.first() shouldBe "Test"
                hotData.data.first() shouldBe "Test"
            }
            coVerify(exactly = 2) { valueProvider.invoke(any()) }
        }
    }

    @Test
    fun `subscription doesn't end when no subscriber is collecting, mode Lazily`() {
        val testScope = TestCoroutineScope()
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
            runBlockingTest2(permanentJobs = true) {
                hotData.data.first() shouldBe "Test"
                hotData.data.first() shouldBe "Test"
            }
            coVerify(exactly = 1) { valueProvider.invoke(any()) }
        }
    }

    @Test
    fun `value updates`() {
        val testScope = TestCoroutineScope()
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
                    hotData.updateSafely {
                        this + 1L
                    }
                }
            }
        }

        runBlocking {
            testCollector.await { list, l -> list.size == 3201 }
            testCollector.latestValues shouldBe (1..3201).toList()
        }

        coVerify(exactly = 1) { valueProvider.invoke(any()) }
    }

    data class TestData(
        val number: Long = 1
    )

    @Test
    fun `check multi threading value updates with more complex data`() {
        val testScope = TestCoroutineScope()
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
                    hotData.updateSafely {
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
            testCollector.await { list, l -> list.size == 4001 }
            testCollector.latestValues.map { it.values.single().number } shouldBe (1L..4001L).toList()
        }

        coVerify(exactly = 1) { valueProvider.invoke(any()) }
    }

    @Test
    fun `only emit new values if they actually changed updates`() {
        val testScope = TestCoroutineScope()

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            startValueProvider = { "1" },
            sharingBehavior = SharingStarted.Lazily
        )

        val testCollector = hotData.data.test(startOnScope = testScope)
        testCollector.silent = true

        hotData.updateSafely { "1" }
        hotData.updateSafely { "2" }
        hotData.updateSafely { "2" }
        hotData.updateSafely { "1" }


        runBlocking {
            testCollector.await { list, l -> list.size == 3 }
            testCollector.latestValues shouldBe listOf("1", "2", "1")
        }
    }

    @Test
    fun `multiple subscribers share the flow`() {
        val testScope = TestCoroutineScope()
        val valueProvider = mockk<suspend CoroutineScope.() -> String>()
        coEvery { valueProvider.invoke(any()) } returns "Test"

        val hotData = HotDataFlow(
            loggingTag = "tag",
            scope = testScope,
            startValueProvider = valueProvider,
            sharingBehavior = SharingStarted.Lazily
        )


        testScope.runBlockingTest2(permanentJobs = true) {
            val sub1 = hotData.data.test(tag = "sub1", startOnScope = this)
            val sub2 = hotData.data.test(tag = "sub2", startOnScope = this)
            val sub3 = hotData.data.test(tag = "sub3", startOnScope = this)

            hotData.updateSafely { "A" }
            hotData.updateSafely { "B" }
            hotData.updateSafely { "C" }

            listOf(sub1, sub2, sub3).forEach {
                it.await { list, s -> list.size == 4 }
                it.latestValues shouldBe listOf("Test", "A", "B", "C")
                it.cancel()
            }

            hotData.data.first() shouldBe "C"
        }
        coVerify(exactly = 1) { valueProvider.invoke(any()) }
    }

    @Test
    fun `update queue is wiped on completion`() = runBlockingTest2(permanentJobs = true) {
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
            hotData.updateSafely {
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
}
