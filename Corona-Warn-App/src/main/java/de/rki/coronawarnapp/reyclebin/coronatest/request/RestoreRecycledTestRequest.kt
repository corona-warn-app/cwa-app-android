package de.rki.coronawarnapp.reyclebin.coronatest.request

import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class RestoreRecycledTestRequest(
    override val type: BaseCoronaTest.Type,
    override val identifier: String,
    override val isDccSupportedByPoc: Boolean,
    override val isDccConsentGiven: Boolean,
    override val dateOfBirth: LocalDate? = null, // Test does not have DoB
    val openResult: Boolean = false
) : TestRegistrationRequest

fun BaseCoronaTest.toRestoreRecycledTestRequest(openResult: Boolean = true) = RestoreRecycledTestRequest(
    type = type,
    identifier = identifier,
    isDccSupportedByPoc = isDccSupportedByPoc,
    isDccConsentGiven = isDccConsentGiven,
    openResult = openResult
)
