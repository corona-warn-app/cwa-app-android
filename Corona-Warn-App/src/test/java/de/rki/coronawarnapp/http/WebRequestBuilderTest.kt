package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.http.service.SubmissionService
import de.rki.coronawarnapp.http.service.VerificationService
import de.rki.coronawarnapp.util.security.VerificationKeys
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before

class WebRequestBuilderTest {
    @MockK
    private lateinit var verificationService: VerificationService

    @MockK
    private lateinit var submissionService: SubmissionService

    @MockK
    private lateinit var verificationKeys: VerificationKeys

    private lateinit var webRequestBuilder: WebRequestBuilder

    @Before
    fun setUp() = run {
        MockKAnnotations.init(this)
        webRequestBuilder = WebRequestBuilder(
            verificationService,
            submissionService
        )
    }

    @After
    fun tearDown() = unmockkAll()
}
