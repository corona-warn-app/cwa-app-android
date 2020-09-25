package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.util.security.SecurityHelper.globalEncryptedSharedPreferencesInstance
import de.rki.coronawarnapp.util.storage.toCommaSeperatedListString
import de.rki.coronawarnapp.util.storage.toIntArray
import org.joda.time.Instant

class PreferencesSubmissionStatusRepository : SubmissionStatusRepository {

    companion object {
        private const val KEY_TIMESTAMP =
            "de.rki.coronawarnapp.submission.PreferencesSubmissionStatusRepository.timestamp"
        private const val KEY_SUCCESS =
            "de.rki.coronawarnapp.submission.PreferencesSubmissionStatusRepository.success"
        private const val KEY_VECTOR =
            "de.rki.coronawarnapp.submission.PreferencesSubmissionStatusRepository.vector"
    }

    override var lastSubmission: SubmissionStatus?
        get() {
            globalEncryptedSharedPreferencesInstance.getLong(KEY_TIMESTAMP, -1).also {
                if (it >= 0) {
                    return SubmissionStatus(
                        Instant.ofEpochMilli(it),
                        globalEncryptedSharedPreferencesInstance.getBoolean(KEY_SUCCESS, false),
                        TransmissionRiskVector(
                            globalEncryptedSharedPreferencesInstance.getString(
                                KEY_VECTOR,
                                ""
                            )?.toIntArray() ?: intArrayOf()
                        )
                    )
                }
                return null
            }
        }
        set(value) { // TODO call this
            globalEncryptedSharedPreferencesInstance.edit()
                .putLong(KEY_TIMESTAMP, value?.timestamp?.millis ?: -1)
                .putBoolean(KEY_SUCCESS, value?.succeeded ?: false)
                .putString(
                    KEY_VECTOR,
                    value?.transmissionRiskVector?.raw?.toCommaSeperatedListString() ?: ""
                )
                .apply()
        }
}
