package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.qrcode.RapidAntigenHash
import de.rki.coronawarnapp.util.CWADebug
import javax.inject.Inject

@Reusable
class RatQrCodeCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(entry: LogLine): LogLine? {

        val rawString = latestScannedRawString ?: return null
        val hash = latestScannedHash ?: return null

        var newMessage = entry.message.replace(rawString, "RatQrCode/ScannedRawString")

        newMessage = if (CWADebug.isDeviceForTestersBuild) {
            newMessage.replace(hash, PLACEHOLDER + hash.takeLast(28))
        } else {
            newMessage.replace(hash, PLACEHOLDER + hash.takeLast(4))
        }

        return entry.toNewLogLineIfDifferent(newMessage)
    }

    companion object {
        var latestScannedRawString: String? = null
            private set

        var latestScannedHash: RapidAntigenHash? = null
            private set

        fun setDataToCensor(rawString: String, hash: RapidAntigenHash) {
            latestScannedRawString = rawString
            latestScannedHash = hash
        }

        fun clearDataToCensor() {
            latestScannedRawString = null
            latestScannedHash = null
        }

        private const val PLACEHOLDER = "SHA256HASH-ENDING-WITH-"
    }
}
