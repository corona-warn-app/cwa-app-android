package de.rki.coronawarnapp.util.security

object SecurityConstants {
    const val EXPORT_FILE_SIGNATURE_VERIFICATION_PUBLIC_KEY_FILES = "export-server-public-keys-for-verification"
    const val EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM = "SHA256withECDSA"
    const val DIGEST_ALGORITHM = "SHA-256"
    const val AES_KEY_SIZE = 256
    const val ANDROID_KEY_STORE = "AndroidKeyStore"
    const val ENCRYPTED_SHARED_PREFERENCES_FILE = "shared_preferences_cwa"
}
