package de.rki.coronawarnapp.datadonation.safetynet

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AttestationContainerTest : BaseTest() {

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun create(rawJson: String) = AttestationContainer(
        ourSalt = ByteArray(16),
        report = SafetyNetClientWrapper.Report(
            jwsResult = "jwsResult",
            header = ObjectMapper().createObjectNode(),
            body = ObjectMapper().readTree(rawJson),
            signature = ByteArray(128)
        )
    )

    @Test
    fun `nothing required`() {
        val attestation =
            """
            {
          
            }
            """.trimIndent().let { create(it) }

        shouldNotThrowAny {
            attestation.requirePass(SafetyNetRequirementsContainer())
        }
    }

    @Test
    fun `basic integrity required`() {
        val attestation =
            """
            {
                "basicIntegrity": false
            }
            """.trimIndent().let { create(it) }

        val exception = shouldThrow<SafetyNetException> {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true
                )
            )
        }
        exception.type shouldBe SafetyNetException.Type.BASIC_INTEGRITY_REQUIRED
    }

    @Test
    fun `basic integrity required bad JSON`() {
        val attestation =
            """
            {
                "basicIntegrity": "test"
            }
            """.trimIndent().let { create(it) }

        val exception = shouldThrow<SafetyNetException> {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true
                )
            )
        }
        exception.type shouldBe SafetyNetException.Type.BASIC_INTEGRITY_REQUIRED
    }

    @Test
    fun `cts profile match required`() {
        val attestation =
            """
            {
                "ctsProfileMatch": false,
                "basicIntegrity": true
            }
            """.trimIndent().let { create(it) }

        val exception = shouldThrow<SafetyNetException> {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true,
                    requireCTSProfileMatch = true
                )
            )
        }
        exception.type shouldBe SafetyNetException.Type.CTS_PROFILE_MATCH_REQUIRED
    }

    @Test
    fun `cts profile match required BAD JSON`() {
        val attestation =
            """
            {
                "ctsProfileMatch": "123",
                "basicIntegrity": true
            }
            """.trimIndent().let { create(it) }

        val exception = shouldThrow<SafetyNetException> {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true,
                    requireCTSProfileMatch = true
                )
            )
        }
        exception.type shouldBe SafetyNetException.Type.CTS_PROFILE_MATCH_REQUIRED
    }

    @Test
    fun `evaluation type basic required`() {
        val attestation =
            """
            {
                "ctsProfileMatch": true,
                "basicIntegrity": true
            }
            """.trimIndent().let { create(it) }

        val exception = shouldThrow<SafetyNetException> {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true,
                    requireCTSProfileMatch = true,
                    requireEvaluationTypeBasic = true
                )
            )
        }
        exception.type shouldBe SafetyNetException.Type.EVALUATION_TYPE_BASIC_REQUIRED
    }

    @Test
    fun `evaluation type basic required BAD JSON`() {
        val attestation =
            """
            {
                "ctsProfileMatch": true,
                "basicIntegrity": true,
                "evaluationType": ""
            }
            """.trimIndent().let { create(it) }

        val exception = shouldThrow<SafetyNetException> {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true,
                    requireCTSProfileMatch = true,
                    requireEvaluationTypeBasic = true
                )
            )
        }
        exception.type shouldBe SafetyNetException.Type.EVALUATION_TYPE_BASIC_REQUIRED
    }

    @Test
    fun `evaluation type hardwarebacked required`() {
        val attestation =
            """
            {
                "ctsProfileMatch": true,
                "basicIntegrity": true,
                "evaluationType": " BASIC "
            }
            """.trimIndent().let { create(it) }

        val exception = shouldThrow<SafetyNetException> {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true,
                    requireCTSProfileMatch = true,
                    requireEvaluationTypeBasic = true,
                    requireEvaluationTypeHardwareBacked = true
                )
            )
        }
        exception.type shouldBe SafetyNetException.Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED
    }

    @Test
    fun `evaluation type hardwarebacked required BAD JSON`() {
        val attestation =
            """
            {
                "ctsProfileMatch": true,
                "basicIntegrity": true,
                "evaluationType": "BASIC, SURPRISE"
            }
            """.trimIndent().let { create(it) }

        val exception = shouldThrow<SafetyNetException> {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true,
                    requireCTSProfileMatch = true,
                    requireEvaluationTypeBasic = true,
                    requireEvaluationTypeHardwareBacked = true
                )
            )
        }
        exception.type shouldBe SafetyNetException.Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED
    }

    @Test
    fun `everything required and pass`() {
        val attestation =
            """
            {
                "ctsProfileMatch": true,
                "basicIntegrity": true,
                "evaluationType": "BASIC,HARDWARE_BACKED"
            }
            """.trimIndent().let { create(it) }

        shouldNotThrowAny {
            attestation.requirePass(
                SafetyNetRequirementsContainer(
                    requireBasicIntegrity = true,
                    requireCTSProfileMatch = true,
                    requireEvaluationTypeBasic = true,
                    requireEvaluationTypeHardwareBacked = true
                )
            )
        }
    }
}
