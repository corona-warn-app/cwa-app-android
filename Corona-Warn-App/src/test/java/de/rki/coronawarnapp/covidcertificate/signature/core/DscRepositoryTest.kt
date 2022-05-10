package de.rki.coronawarnapp.covidcertificate.signature.core

import de.rki.coronawarnapp.covidcertificate.signature.core.server.DscServer
import de.rki.coronawarnapp.covidcertificate.signature.core.storage.DefaultDscSource
import de.rki.coronawarnapp.covidcertificate.signature.core.storage.LocalDscStorage
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2
import java.lang.Exception

internal class DscRepositoryTest {

    @MockK lateinit var defaultDscData: DefaultDscSource
    @MockK lateinit var localStorage: LocalDscStorage
    @MockK lateinit var dscServer: DscServer
    @MockK lateinit var dscDataParser: DscDataParser

    private val emptyDscList = DscSignatureList(listOf(), Instant.EPOCH)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { localStorage.load() } returns null
        every { defaultDscData.getDscData() } returns emptyDscList
        coEvery { dscServer.getDscList() } returns byteArrayOf()
        every { dscDataParser.parse(any(), any()) } returns emptyDscList
        coEvery { localStorage.save(any()) } just Runs
    }

    @Test
    fun `local cache is loaded from default storage - no server requests`() = runTest2 {
        createInstance(this).apply {
            dscSignatureList.first() shouldBe emptyDscList
        }

        coVerify {
            localStorage.load()
        }

        verify {
            defaultDscData.getDscData()
        }

        coVerify(exactly = 0) {
            dscServer.getDscList()
        }
    }

    @Test
    fun `local cache is loaded from local storage - no server requests`() = runTest2 {
        coEvery { localStorage.load() } returns emptyDscList

        createInstance(this).apply {
            dscSignatureList.first() shouldBe emptyDscList
        }

        coVerify {
            localStorage.load()
        }

        verify(exactly = 0) {
            defaultDscData.getDscData()
        }

        coVerify(exactly = 0) {
            dscServer.getDscList()
        }
    }

    @Test
    fun `refresh talks to server and updates local cache`() = runTest2 {
        createInstance(this).apply {
            refresh()

            dscSignatureList.first() shouldBe emptyDscList

            coVerify {
                dscServer.getDscList()
                localStorage.save(any())
            }
        }
    }

    @Test
    fun `bad server response yields exception and data are not saved to local repository`() =
        runTest2 {

            every { dscDataParser.parse(any()) } throws Exception()

            shouldThrowAny {
                createInstance(this).apply {
                    refresh()

                    coVerify {
                        dscServer.getDscList()
                    }

                    verify {
                        dscDataParser.parse(any())
                    }

                    coVerify(exactly = 0) {
                        localStorage.save(any())
                    }
                }
            }
        }

    private fun createInstance(scope: CoroutineScope) = DscRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        defaultDscData = defaultDscData,
        localStorage = localStorage,
        dscServer = dscServer,
        dscDataParser = dscDataParser
    )
}
