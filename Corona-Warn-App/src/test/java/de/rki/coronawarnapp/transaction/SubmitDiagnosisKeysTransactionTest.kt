package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.server.protocols.KeyExportFormat
import de.rki.coronawarnapp.service.submission.SubmissionService
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.ExposureKeyHistoryCalculations
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.ApplicationComponent
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class SubmitDiagnosisKeysTransactionTest {

    @MockK lateinit var backgroundNoise: BackgroundNoise
    @MockK lateinit var mockPlaybook: Playbook
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var appComponent: ApplicationComponent
    @MockK lateinit var exposureKeyHistoryCalculations: ExposureKeyHistoryCalculations

    private val registrationToken = "123"

    private val symptoms = Symptoms(Symptoms.StartOf.OneToTwoWeeksAgo, Symptoms.Indication.POSITIVE)
    private val defaultCountries = listOf("DE", "NL", "FR")

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        val appConfig = ApplicationConfiguration.newBuilder()
            .addAllSupportedCountries(defaultCountries)
            .build()
        coEvery { appConfigProvider.getAppConfig() } returns appConfig

        every { appComponent.transSubmitDiagnosisInjection } returns SubmitDiagnosisInjectionHelper(
            TransactionCoroutineScope(),
            mockPlaybook,
            appConfigProvider,
            exposureKeyHistoryCalculations
        )
        mockkObject(AppInjector)
        every { AppInjector.component } returns appComponent

        every {
            exposureKeyHistoryCalculations.transformToKeyHistoryInExternalFormat(
                any(),
                any()
            )
        } returns emptyList()

        mockkObject(BackgroundNoise.Companion)
        every { BackgroundNoise.getInstance() } returns backgroundNoise

        mockkObject(LocalData)
        mockkObject(SubmissionService)
        mockkObject(BackgroundWorkScheduler)
        every { BackgroundWorkScheduler.stopWorkScheduler() } just Runs
        every { LocalData.numberOfSuccessfulSubmissions(any()) } just Runs
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
    }

    @Test
    fun `submission without keys`(): Unit = runBlocking {
        coEvery { mockPlaybook.submission(any()) } returns Unit

        SubmitDiagnosisKeysTransaction.start(registrationToken, listOf(), symptoms)

        coVerifySequence {
            appConfigProvider.getAppConfig()
            mockPlaybook.submission(
                Playbook.SubmissionData(
                    registrationToken = registrationToken,
                    temporaryExposureKeys = emptyList(),
                    consentToFederation = true,
                    visistedCountries = defaultCountries
                )
            )
            SubmissionService.submissionSuccessful()
        }
    }

    @Test
    fun `submission without keys and fallback country`(): Unit = runBlocking {
        val appConfig = ApplicationConfiguration.newBuilder().build()
        coEvery { appConfigProvider.getAppConfig() } returns appConfig
        coEvery { mockPlaybook.submission(any()) } returns Unit

        SubmitDiagnosisKeysTransaction.start(registrationToken, listOf(), symptoms)

        coVerifySequence {
            appConfigProvider.getAppConfig()
            mockPlaybook.submission(
                Playbook.SubmissionData(
                    registrationToken = registrationToken,
                    temporaryExposureKeys = emptyList(),
                    consentToFederation = true,
                    visistedCountries = listOf("DE")
                )
            )
            SubmissionService.submissionSuccessful()
        }
    }

    @Test
    fun `submission with keys`() {
        val intervalNumber = (Instant.now().toEpochMilli() / (60 * 10 * 1000)).toInt()
        runBlocking {
            val key = TemporaryExposureKey.TemporaryExposureKeyBuilder()
                .setKeyData(ByteArray(1))
                .setRollingPeriod(144)
                .setRollingStartIntervalNumber(intervalNumber)
                .setTransmissionRiskLevel(1)
                .setDaysSinceOnsetOfSymptoms(10)
                .build()

            every {
                exposureKeyHistoryCalculations.transformToKeyHistoryInExternalFormat(
                    any(),
                    any()
                )
            } returns listOf(
                KeyExportFormat.TemporaryExposureKey.newBuilder()
                    .setKeyData(ByteString.copyFrom(ByteArray(1)))
                    .setRollingPeriod(144)
                    .setRollingStartIntervalNumber(intervalNumber)
                    .setTransmissionRiskLevel(1)
                    .setDaysSinceOnsetOfSymptoms(10)
                    .build()
            )

            coEvery { mockPlaybook.submission(any()) } answers {
                arg<Playbook.SubmissionData>(0).also {
                    it.registrationToken shouldBe registrationToken
                    it.temporaryExposureKeys.single().apply {
                        keyData.toByteArray() shouldBe ByteArray(1)
                        rollingPeriod shouldBe 144
                        rollingStartIntervalNumber shouldBe intervalNumber
                        transmissionRiskLevel shouldBe 1
                        daysSinceOnsetOfSymptoms shouldBe 10
                    }
                    it.consentToFederation shouldBe true
                    it.visistedCountries shouldBe defaultCountries
                }
                Unit
            }

            SubmitDiagnosisKeysTransaction.start(registrationToken, listOf(key), symptoms)

            coVerifySequence {
                appConfigProvider.getAppConfig()
                mockPlaybook.submission(any())
                SubmissionService.submissionSuccessful()
            }
        }
    }
}
