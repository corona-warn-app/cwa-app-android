package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class BackgroundNoise @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
    private val playbook: Playbook,
) {

    suspend fun foregroundScheduleCheck() {
        val isAllowedToSubmitKeys = coronaTestRepository.coronaTests.first().any { it.isSubmissionAllowed }
        if (isAllowedToSubmitKeys) {
            val chance = Random.nextFloat() * 100
            if (chance < PROBABILITY_TO_EXECUTE_PLAYBOOK_ON_APP_OPEN) {
                playbook.dummy()
            }
        }
    }
}

const val PROBABILITY_TO_EXECUTE_PLAYBOOK_ON_APP_OPEN = 0f
