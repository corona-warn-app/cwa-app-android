package de.rki.coronawarnapp.srs.core

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.google.protobuf.ByteString
import dagger.Reusable
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.toProtoByteString
import okio.ByteString.Companion.decodeHex
import timber.log.Timber
import javax.inject.Inject

@Reusable
class AndroidIdProvider @Inject constructor(
    @AppContext private val context: Context
) {

    @SuppressLint("HardwareIds")
    fun getAndroidId(): ByteString =
        try {
            Timber.d("getAndroidId()")
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ).decodeHex().toProtoByteString()
        } catch (e: Exception) {
            Timber.e(e, "getAndroidId() failed")
            throw SrsSubmissionException(
                errorCode = SrsSubmissionException.ErrorCode.ANDROID_ID_INVALID_LOCAL,
                cause = e
            )
        }
}
