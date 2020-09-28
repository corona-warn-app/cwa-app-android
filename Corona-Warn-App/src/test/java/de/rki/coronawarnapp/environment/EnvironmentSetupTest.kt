package de.rki.coronawarnapp.environment

import android.content.Context
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.util.CWADebug
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import testhelpers.BaseTest

class EnvironmentSetupTest : BaseTest() {
    @MockK
    var context: Context = mock(Context::class.java)
    private fun createEnvironmentSetup() = EnvironmentSetup(context)
    private val cdnUrlSubmission = "SUBMISSION_CDN_URL"
    private val cdnUrlDownload = "DOWNLOAD_CDN_URL"
    private val cdnUrlVerification = "VERIFICATION_CDN_URL"
    private val environmentType = "DEV"
    private val badJson = "{ \"$environmentType\": {\n \"SUBMISSION_CDN_U"
    private val goodJson = "{\n  \"DEV\": {\n    \"$cdnUrlSubmission\": \"https://submission-dev\",\n    \"$cdnUrlDownload\": \"https://download-dev\",\n    \"$cdnUrlVerification\": \"https://verification-dev\"\n  },\n  \"INT\": {\n    \"$cdnUrlSubmission\": \"https://submission-int\",\n    \"$cdnUrlDownload\": \"https://download-int\",\n    \"$cdnUrlVerification\": \"https://verification-int\"\n  },\n  \"WRU\": {\n   \"$cdnUrlSubmission\": \"https://submission-wru\",\n    \"$cdnUrlDownload\": \"https://download-wru\",\n    \"$cdnUrlVerification\": \"https://verification-wru\"\n  },\n  \"WRU-XD\": {\n  \"$cdnUrlSubmission\": \"https://submission-wru-xd\",\n    \"$cdnUrlDownload\": \"https://download-wru-xd\",\n    \"$cdnUrlVerification\": \"https://verification-wru-xd\"\n  },\n  \"WRU-XA\": {\n   \"$cdnUrlSubmission\": \"https://submission-wru-xa\",\n    \"$cdnUrlDownload\": \"https://download-wru-xa\",\n    \"$cdnUrlVerification\": \"https://verification-wru-xa\"\n  }\n}\n"
        @MockK
    private lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp(){
        MockKAnnotations.init(this)
        mockkObject(BuildConfigWrap)
        mockkObject(CWADebug)
        every { context.getSharedPreferences() }
    }


    @Test
    fun `test mocking BuildConfig`() {
        BuildConfigWrap.DOWNLOAD_CDN_URL shouldBe BuildConfig.DOWNLOAD_CDN_URL
        every { BuildConfigWrap.DOWNLOAD_CDN_URL } returns cdnUrlDownload
        BuildConfigWrap.DOWNLOAD_CDN_URL shouldBe cdnUrlDownload

        BuildConfigWrap.VERIFICATION_CDN_URL shouldBe BuildConfig.VERIFICATION_CDN_URL
        every { BuildConfigWrap.VERIFICATION_CDN_URL } returns cdnUrlVerification
        BuildConfigWrap.VERIFICATION_CDN_URL shouldBe cdnUrlVerification


        BuildConfigWrap.SUBMISSION_CDN_URL shouldBe BuildConfig.SUBMISSION_CDN_URL
        every { BuildConfigWrap.SUBMISSION_CDN_URL } returns cdnUrlSubmission
        BuildConfigWrap.SUBMISSION_CDN_URL shouldBe cdnUrlSubmission

        BuildConfigWrap.TEST_ENVIRONMENT_JSONDATA shouldBe BuildConfig.TEST_ENVIRONMENT_JSONDATA
        every { BuildConfigWrap.TEST_ENVIRONMENT_JSONDATA } returns BuildConfig.TEST_ENVIRONMENT_JSONDATA
        BuildConfigWrap.TEST_ENVIRONMENT_JSONDATA shouldBe BuildConfig.TEST_ENVIRONMENT_JSONDATA
    }

    @Test
    fun `parsing bad json throws an exception in debug builds`() {
        every { CWADebug.isDebugBuildOrMode } returns true
        every { BuildConfigWrap.TEST_ENVIRONMENT_JSONDATA } returns badJson
        shouldThrowAny {
            createEnvironmentSetup().currentEnvironment
        }
    }

    @Test
    fun `parsing bad json does not throw exception non-debug builds`() {
        every { CWADebug.isDebugBuildOrMode } returns false
        every { BuildConfigWrap.TEST_ENVIRONMENT_JSONDATA } returns badJson
        shouldNotThrowAny {
            createEnvironmentSetup().currentEnvironment
        }
    }

    @Test
    fun `mapping between function and JSON variable names is correct`() {
        every { BuildConfigWrap.TEST_ENVIRONMENT_JSONDATA } returns goodJson
            createEnvironmentSetup().currentEnvironment.rawKey shouldBe environmentType
    }

    @Test
    fun `default environment type is set correctly`() {
        every { BuildConfigWrap.TEST_ENVIRONMENT_DEFAULTTYPE } returns BuildConfig.TEST_ENVIRONMENT_DEFAULT_TYPE
        createEnvironmentSetup().defaultEnvironment.rawKey shouldBe BuildConfig.TEST_ENVIRONMENT_DEFAULT_TYPE
    }

    @Test
    fun `switching the current environment type causes new urls to be returned`() {
        every { CWADebug.isDebugBuildOrMode } returns true
        every { BuildConfigWrap.TEST_ENVIRONMENT_JSONDATA } returns goodJson
        createEnvironmentSetup().cdnUrlDownload shouldBe cdnUrlDownload
        createEnvironmentSetup().cdnUrlSubmission shouldBe cdnUrlSubmission
        createEnvironmentSetup().cdnUrlVerification shouldBe cdnUrlVerification
    }

    @Test
    fun `switching the default type is persisted in storage (preferences)`() {
        every { BuildConfigWrap.TEST_ENVIRONMENT_DEFAULTTYPE } returns environmentType
        createEnvironmentSetup().defaultEnvironment shouldBe environmentType
        createEnvironmentSetup().currentEnvironment = EnvironmentSetup.Type.WRU_XA
        createEnvironmentSetup().defaultEnvironment shouldBe EnvironmentSetup.Type.WRU_XA.rawKey
    }
}
