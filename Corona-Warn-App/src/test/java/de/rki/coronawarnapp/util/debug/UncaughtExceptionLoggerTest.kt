package de.rki.coronawarnapp.util.debug

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class UncaughtExceptionLoggerTest : BaseTest() {

    var originalHandler: Thread.UncaughtExceptionHandler? = null

    @BeforeEach
    fun setup() {
        originalHandler = Thread.getDefaultUncaughtExceptionHandler()
    }

    @AfterEach
    fun teardown() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
    }

    @Test
    fun `we wrap and call through to the original handler`() {
        val wrappedHandler = mockk<Thread.UncaughtExceptionHandler>()
        every { wrappedHandler.uncaughtException(any(), any()) } just Runs

        val instance = UncaughtExceptionLogger(wrappedHandler)
        val testException = NotImplementedError()
        instance.uncaughtException(Thread.currentThread(), testException)

        verify { wrappedHandler.uncaughtException(Thread.currentThread(), testException) }
    }

    @Test
    fun `auto setup replaces the current handler`() {
        val wrappedHandler = mockk<Thread.UncaughtExceptionHandler>()
        every { wrappedHandler.uncaughtException(any(), any()) } just Runs

        Thread.setDefaultUncaughtExceptionHandler(wrappedHandler)
        Thread.getDefaultUncaughtExceptionHandler() shouldBe wrappedHandler

        val ourHandler = UncaughtExceptionLogger.wrapCurrentHandler()
        Thread.getDefaultUncaughtExceptionHandler() shouldBe ourHandler
    }

    @Test
    fun `null handlers would be okay`() {
        Thread.setDefaultUncaughtExceptionHandler(null)
        Thread.getDefaultUncaughtExceptionHandler() shouldBe null

        val ourHandler = UncaughtExceptionLogger.wrapCurrentHandler()
        Thread.getDefaultUncaughtExceptionHandler() shouldBe ourHandler

        val instance = UncaughtExceptionLogger(null)
        instance.uncaughtException(Thread.currentThread(), NotImplementedError())
    }
}
