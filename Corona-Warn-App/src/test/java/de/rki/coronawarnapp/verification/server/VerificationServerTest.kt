package de.rki.coronawarnapp.verification.server

import dagger.Lazy
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class VerificationServerTest : BaseIOTest() {

    @MockK
    lateinit var api: DiagnosisKeyApiV1

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private val defaultHomeCountry = LocationCode("DE")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createDownloadServer(
        homeCountry: LocationCode = defaultHomeCountry
    ) = DiagnosisKeyServer(
        diagnosisKeyAPI = Lazy { api },
        homeCountry = homeCountry
    )

    @Test
    fun `todo`() {
        TODO()
    }
}
