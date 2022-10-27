package testhelpers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.covidcertificate.CovidCertificateSettingsDataStore
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsSettingsDataStore
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionDataStore
import de.rki.coronawarnapp.datadonation.analytics.modules.testresult.AnalyticsExposureWindowsDataStore
import de.rki.coronawarnapp.datadonation.survey.SurveySettingsDataStore
import de.rki.coronawarnapp.main.CwaSettingsDataStore
import de.rki.coronawarnapp.presencetracing.LocationPreferencesDataStore
import de.rki.coronawarnapp.presencetracing.LocationSettingsDataStore
import de.rki.coronawarnapp.risk.RiskLevelSettingsDataStore
import de.rki.coronawarnapp.statistics.LocalStatisticsConfigDataStore
import de.rki.coronawarnapp.storage.OnboardingSettingsDataStore
import de.rki.coronawarnapp.storage.TestSettingsDataStore
import de.rki.coronawarnapp.storage.TracingSettingsDataStore
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.di.AppInstallTime
import io.mockk.mockk
import java.time.Instant
import javax.inject.Singleton

@Module
class TestAndroidModule {
    @Provides
    @Singleton
    @AppContext
    fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Provides
    @AppInstallTime
    fun installTime(@AppContext context: Context): Instant = Instant.EPOCH

    @OnboardingSettingsDataStore
    @Provides
    fun provideOnboardingSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @TestSettingsDataStore
    @Provides
    fun provideTestSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @TracingSettingsDataStore
    @Provides
    fun provideTracingSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @SurveySettingsDataStore
    @Provides
    fun provideSurveySettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @RiskLevelSettingsDataStore
    @Provides
    fun provideRiskLevelSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @CwaSettingsDataStore
    @Provides
    fun provideCwaSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @AnalyticsSettingsDataStore
    @Provides
    fun provideAnalyticsSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @CovidCertificateSettingsDataStore
    @Provides
    fun provideCovidCertificateSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @LocationSettingsDataStore
    @Provides
    fun provideLocationSettingsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @LocationPreferencesDataStore
    @Provides
    fun provideLocationPreferencesDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @AnalyticsKeySubmissionDataStore
    @Provides
    fun provideAnalyticsKeySubmissionDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @AnalyticsExposureWindowsDataStore
    @Provides
    fun provideAnalyticsExposureWindowsDataStore(): DataStore<Preferences> = mockk(relaxed = true)

    @LocalStatisticsConfigDataStore
    @Provides
    fun provideLocalStatisticsConfigDataStore(): DataStore<Preferences> = mockk(relaxed = true)
}
