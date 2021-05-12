package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryPreferences
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettings
import de.rki.coronawarnapp.datadonation.analytics.Analytics
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.survey.SurveySettings
import de.rki.coronawarnapp.diagnosiskeys.download.DownloadDiagnosisKeysSettings
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.nearby.modules.detectiontracker.ExposureDetectionTracker
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.statistics.source.StatisticsProvider
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.ui.presencetracing.TraceLocationPreferences
import de.rki.coronawarnapp.vaccination.core.VaccinationPreferences
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DataResetTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var keyCacheRepository: KeyCacheRepository
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var submissionRepository: SubmissionRepository
    @MockK lateinit var exposureDetectionTracker: ExposureDetectionTracker
    @MockK lateinit var downloadDiagnosisKeysSettings: DownloadDiagnosisKeysSettings
    @MockK lateinit var riskLevelStorage: RiskLevelStorage
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository
    @MockK lateinit var contactDiaryPreferences: ContactDiaryPreferences
    @MockK lateinit var traceLocationPreferences: TraceLocationPreferences
    @MockK lateinit var vaccinationPreferences: VaccinationPreferences
    @MockK lateinit var cwaSettings: CWASettings
    @MockK lateinit var statisticsProvider: StatisticsProvider
    @MockK lateinit var surveySettings: SurveySettings
    @MockK lateinit var analyticsSettings: AnalyticsSettings
    @MockK lateinit var analytics: Analytics
    @MockK lateinit var bugReportingSettings: BugReportingSettings
    @MockK lateinit var tracingSettings: TracingSettings
    @MockK lateinit var onboardingSettings: OnboardingSettings
    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var traceLocationRepository: TraceLocationRepository
    @MockK lateinit var traceWarningRepository: TraceWarningRepository
    @MockK lateinit var checkInRepository: CheckInRepository
    @MockK lateinit var traceLocationSettings: TraceLocationSettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository
    @MockK lateinit var ratProfileSettings: RATProfileSettings

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
    }

    fun createInstance() = DataReset(
        context = context,
        keyCacheRepository = keyCacheRepository,
        appConfigProvider = appConfigProvider,
        submissionRepository = submissionRepository,
        exposureDetectionTracker = exposureDetectionTracker,
        downloadDiagnosisKeysSettings = downloadDiagnosisKeysSettings,
        riskLevelStorage = riskLevelStorage,
        contactDiaryRepository = contactDiaryRepository,
        traceLocationPreferences = traceLocationPreferences,
        contactDiaryPreferences = contactDiaryPreferences,
        cwaSettings = cwaSettings,
        statisticsProvider = statisticsProvider,
        surveySettings = surveySettings,
        analyticsSettings = analyticsSettings,
        analytics = analytics,
        bugReportingSettings = bugReportingSettings,
        tracingSettings = tracingSettings,
        onboardingSettings = onboardingSettings,
        submissionSettings = submissionSettings,
        traceLocationRepository = traceLocationRepository,
        checkInRepository = checkInRepository,
        traceLocationSettings = traceLocationSettings,
        traceWarningRepository = traceWarningRepository,
        coronaTestRepository = coronaTestRepository,
        ratProfileSettings = ratProfileSettings,
        vaccinationPreferences = vaccinationPreferences
    )

    @Test
    fun `clearAllLocalData() should clear all data`() = runBlockingTest {
        createInstance().clearAllLocalData()

        coVerify(exactly = 1) { analytics.setAnalyticsEnabled(false) }

        coVerify(exactly = 1) { submissionRepository.reset() }
        coVerify(exactly = 1) { keyCacheRepository.clear() }
        coVerify(exactly = 1) { appConfigProvider.clear() }
        coVerify(exactly = 1) { exposureDetectionTracker.clear() }
        coVerify(exactly = 1) { downloadDiagnosisKeysSettings.clear() }
        coVerify(exactly = 1) { riskLevelStorage.clear() }
        coVerify(exactly = 1) { contactDiaryPreferences.clear() }
        coVerify(exactly = 1) { traceLocationPreferences.clear() }
        coVerify(exactly = 1) { vaccinationPreferences.clear() }
        coVerify(exactly = 1) { cwaSettings.clear() }
        coVerify(exactly = 1) { surveySettings.clear() }
        coVerify(exactly = 1) { analyticsSettings.clear() }
        coVerify(exactly = 1) { tracingSettings.clear() }
        coVerify(exactly = 1) { onboardingSettings.clear() }
        coVerify(exactly = 1) { submissionSettings.clear() }
        coVerify(exactly = 1) { traceLocationSettings.clear() }

        coVerify(exactly = 1) { contactDiaryRepository.clear() }

        coVerify(exactly = 1) { statisticsProvider.clear() }

        coVerify(exactly = 1) { bugReportingSettings.clear() }
        coVerify(exactly = 1) { traceWarningRepository.clear() }
        coVerify(exactly = 1) { traceLocationRepository.deleteAllTraceLocations() }
        coVerify(exactly = 1) { checkInRepository.clear() }
        coVerify(exactly = 1) { coronaTestRepository.clear() }
        coVerify(exactly = 1) { ratProfileSettings.deleteProfile() }
    }
}
