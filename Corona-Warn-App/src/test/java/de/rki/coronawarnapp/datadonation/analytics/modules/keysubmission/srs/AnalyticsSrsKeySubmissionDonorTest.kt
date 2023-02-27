package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPAKeySubmissionMetadata
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class AnalyticsSrsKeySubmissionDonorTest : BaseTest() {

    @MockK lateinit var repo: AnalyticsSrsKeySubmissionRepository
    @MockK lateinit var builder: PpaData.PPADataAndroid.Builder
    @MockK lateinit var configData: ConfigData

    private val ppaData = PPAKeySubmissionMetadata.newBuilder().build()
    private lateinit var request: DonorModule.Request

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { repo.reset() } just Runs
        coEvery { repo.collectSrsSubmissionAnalytics(any(), any()) } just Runs
        coEvery { repo.srsPpaData() } returns ppaData
        every { builder.addKeySubmissionMetadataSet(any<PPAKeySubmissionMetadata>()) } returns builder

        request = object : DonorModule.Request {
            override val currentConfig: ConfigData = configData
        }
    }

    @Test
    fun `beginDonation with data`() = runTest {
        instance().beginDonation(request).injectData(builder)
        verify { builder.addKeySubmissionMetadataSet(ppaData) }
    }

    @Test
    fun `beginDonation on success`() = runTest {
        instance().beginDonation(request).finishDonation(true)
        coVerify { repo.reset() }
    }

    @Test
    fun `beginDonation on failure`() = runTest {
        instance().beginDonation(request).finishDonation(false)
        coVerify(exactly = 0) { repo.reset() }
    }

    @Test
    fun `beginDonation with no data`() = runTest {
        coEvery { repo.srsPpaData() } returns null
        instance().beginDonation(request) shouldBe AnalyticsSrsKeySubmissionDonor.NoContribution
    }

    @Test
    fun deleteData() = runTest {
        instance().deleteData()
        coVerify { repo.reset() }
    }

    private fun instance() = AnalyticsSrsKeySubmissionDonor(
        repository = repo
    )
}
