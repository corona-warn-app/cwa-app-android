package de.rki.coronawarnapp.util.security

import de.rki.coronawarnapp.BuildConfig

object SecurityConstants {
    const val DIGEST_ALGORITHM = "SHA-256"
    const val AES_KEY_SIZE = 256
    const val ANDROID_KEY_STORE = "AndroidKeyStore"
    const val ENCRYPTED_SHARED_PREFERENCES_FILE = "shared_preferences_cwa"

    const val EXPORT_SIGNATURE_VERIFICATION_PUBLIC_KEYS =
        "export-server-public-keys-for-verification.properties"
    const val EXPORT_ENVIRONMENT_IDENTIFIER = BuildConfig.EXPORT_SIGNATURE_ID
    const val EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM = "SHA256withECDSA"
}
