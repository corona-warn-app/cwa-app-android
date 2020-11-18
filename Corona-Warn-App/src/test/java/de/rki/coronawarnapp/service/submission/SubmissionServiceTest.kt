package de.rki.coronawarnapp.service.submission

class SubmissionServiceTest {

//    private val guid = "123456-12345678-1234-4DA7-B166-B86D85475064"
//    private val registrationToken = "asdjnskjfdniuewbheboqudnsojdff"
//    private val testResult = TestResult.PENDING
//
//    @MockK lateinit var backgroundNoise: BackgroundNoise
//    @MockK lateinit var mockPlaybook: Playbook
//    @MockK lateinit var appComponent: ApplicationComponent
//    @MockK lateinit var submissionSettings: SubmissionSettings
//
//    lateinit var submissionRepository: SubmissionRepository
//
//    private val symptoms = Symptoms(Symptoms.StartOf.OneToTwoWeeksAgo, Symptoms.Indication.POSITIVE)
//
//    @BeforeEach
//    fun setUp() {
//        MockKAnnotations.init(this)
//
//        mockkObject(AppInjector)
//        every { AppInjector.component } returns appComponent
//
//        every { appComponent.playbook } returns mockPlaybook
//
//        mockkObject(BackgroundNoise.Companion)
//        every { BackgroundNoise.getInstance() } returns backgroundNoise
//
//        mockkObject(LocalData)
//
//        every { LocalData.teletan() } returns null
//        every { LocalData.testGUID() } returns null
//        every { LocalData.registrationToken() } returns null
//
//        submissionRepository = SubmissionRepository(submissionSettings)
//    }
//
//    @AfterEach
//    fun cleanUp() {
//        clearAllMocks()
//    }
//
//    @Test
//    fun registrationWithGUIDSucceeds() {
//        every { LocalData.testGUID() } returns guid
//
//        every { LocalData.testGUID(any()) } just Runs
//        every { LocalData.registrationToken(any()) } just Runs
//        every { LocalData.devicePairingSuccessfulTimestamp(any()) } just Runs
//
//        coEvery {
//            mockPlaybook.initialRegistration(any(), VerificationKeyType.GUID)
//        } returns (registrationToken to TestResult.PENDING)
//        coEvery { mockPlaybook.testResult(registrationToken) } returns testResult
//
//        every { backgroundNoise.scheduleDummyPattern() } just Runs
//
//        runBlocking {
//            submissionRepository.asyncRegisterDeviceViaGUID(guid)
//        }
//
//        verify(exactly = 1) {
//            LocalData.registrationToken(registrationToken)
//            LocalData.devicePairingSuccessfulTimestamp(any())
//            LocalData.testGUID(null)
//            backgroundNoise.scheduleDummyPattern()
//            submissionRepository.updateTestResult(testResult)
//        }
//    }
//
//    @Test
//    fun registrationWithTeleTANSucceeds() {
//        every { LocalData.teletan() } returns guid
//
//        every { LocalData.teletan(any()) } just Runs
//        every { LocalData.registrationToken(any()) } just Runs
//        every { LocalData.devicePairingSuccessfulTimestamp(any()) } just Runs
//
//        coEvery {
//            mockPlaybook.initialRegistration(any(), VerificationKeyType.TELETAN)
//        } returns (registrationToken to TestResult.PENDING)
//        coEvery { mockPlaybook.testResult(registrationToken) } returns testResult
//
//        every { backgroundNoise.scheduleDummyPattern() } just Runs
//
//        runBlocking {
//            submissionRepository.asyncRegisterDeviceViaTAN(guid)
//        }
//
//        verify(exactly = 1) {
//            LocalData.registrationToken(registrationToken)
//            LocalData.devicePairingSuccessfulTimestamp(any())
//            LocalData.teletan(null)
//            backgroundNoise.scheduleDummyPattern()
//            submissionRepository.updateTestResult(testResult)
//        }
//    }
//
//    @Test
//    fun requestTestResultWithoutRegistrationTokenFails(): Unit = runBlocking {
//        shouldThrow<NoRegistrationTokenSetException> {
//            submissionRepository.asyncRequestTestResult()
//        }
//    }
//
//    @Test
//    fun requestTestResultSucceeds() {
//        every { LocalData.registrationToken() } returns registrationToken
//        coEvery { mockPlaybook.testResult(registrationToken) } returns TestResult.NEGATIVE
//
//        runBlocking {
//            submissionRepository.asyncRequestTestResult() shouldBe TestResult.NEGATIVE
//        }
//    }
//
//    @Test
//    fun deleteRegistrationTokenSucceeds() {
//        every { LocalData.registrationToken(null) } just Runs
//        every { LocalData.devicePairingSuccessfulTimestamp(0L) } just Runs
//
//        submissionRepository.deleteRegistrationToken()
//
//        verify(exactly = 1) {
//            LocalData.registrationToken(null)
//            LocalData.devicePairingSuccessfulTimestamp(0L)
//        }
//    }
}
