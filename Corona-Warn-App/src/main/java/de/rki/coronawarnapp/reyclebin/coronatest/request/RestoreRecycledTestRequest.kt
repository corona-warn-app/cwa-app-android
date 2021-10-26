package de.rki.coronawarnapp.reyclebin.coronatest.request

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import kotlinx.parcelize.Parcelize
import org.joda.time.LocalDate

@Parcelize
data class RestoreRecycledTestRequest(
    override val type: CoronaTest.Type,
    override val identifier: String,
    override val isDccSupportedByPoc: Boolean,
    override val isDccConsentGiven: Boolean,
    override val dateOfBirth: LocalDate? = null // Test does not have DoB
) : TestRegistrationRequest

fun CoronaTest.toRestoreRecycledTestRequest() = RestoreRecycledTestRequest(
    type = type,
    identifier = identifier,
    isDccSupportedByPoc = isDccSupportedByPoc,
    isDccConsentGiven = isDccConsentGiven
)
