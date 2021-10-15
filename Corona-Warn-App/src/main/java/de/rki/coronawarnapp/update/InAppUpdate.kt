package de.rki.coronawarnapp.update

import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { cont ->
        addOnSuccessListener { info ->
            Timber.tag(TAG).d("OnSuccess info=$info")
            cont.resume(info as T)
        }
        addOnFailureListener {
            Timber.tag(TAG).e("OnFailure error=$it")
            cont.resumeWithException(it)
        }
    }
}

suspend fun AppUpdateManager.getUpdateInfo(): AppUpdateInfo? {
    Timber.tag(TAG).d("getUpdateInfo()")
    return try {
        appUpdateInfo.await()
    } catch (e: Exception) {
        Timber.tag(TAG).w(e, "getUpdateInfo() failed")
        null
    }
}

private const val TAG = "InAppUpdate"
