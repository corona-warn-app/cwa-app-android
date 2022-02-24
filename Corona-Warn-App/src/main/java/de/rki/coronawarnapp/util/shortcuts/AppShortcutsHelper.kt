package de.rki.coronawarnapp.util.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.ui.launcher.LauncherActivity
import de.rki.coronawarnapp.util.AppShortcuts
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppShortcutsHelper @Inject constructor(
    @AppContext private val context: Context,
    private val onboardingSettings: OnboardingSettings,
    @AppScope private val appScope: CoroutineScope
) {

    private val scannerShortcut by lazy {
        ShortcutInfoCompat.Builder(context, QR_CODE_SCANNER_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_scanner_title))
            .setLongLabel(context.getString(R.string.app_shortcut_scanner_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_qr_code_scanner_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.QR_CODE_SCANNER.toString()))
            .setRank(0)
            .build()
    }

    private val certificatesShortcut by lazy {
        ShortcutInfoCompat.Builder(context, CERTIFICATES_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_certificates_title))
            .setLongLabel(context.getString(R.string.app_shortcut_certificates_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_certificates_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CERTIFICATES.toString()))
            .setRank(1)
            .build()
    }

    private val checkInShortcut by lazy {
        ShortcutInfoCompat.Builder(context, CHECK_INS_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_check_ins_title))
            .setLongLabel(context.getString(R.string.app_shortcut_check_ins_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_check_ins_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CHECK_INS.toString()))
            .setRank(2)
            .build()
    }

    private val diaryShortcut by lazy {
        ShortcutInfoCompat.Builder(context, CONTACT_DIARY_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setLongLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_contact_diary_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CONTACT_DIARY.toString()))
            .setRank(3)
            .build()
    }

    private val allShortcuts = listOf(scannerShortcut, certificatesShortcut, checkInShortcut, diaryShortcut)

    fun initShortcuts() = appScope.launch {
        Timber.d("initShortcuts()")

        if (!shortcutsAdded()) {
            addShortcuts()
        }

        maybeDisableShortcuts()
    }

    private fun shortcutsAdded() = ShortcutManagerCompat.getDynamicShortcuts(context).containsAll(allShortcuts)

    private fun addShortcuts() {
        allShortcuts.forEach { shortcut ->
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        }
    }

    private fun maybeDisableShortcuts() {
        if (!isOnboarded()) {
            Timber.i("User is not onboarded yet")
            disableAllShortcuts()
        }

        if (isCameraPermissionGranted()) {
            enableQrCodeScannerShortcut()
        } else {
            disableQrCodeScannerShortcut()
        }
    }

    private fun isOnboarded() = onboardingSettings.isOnboarded

    fun disableAllShortcuts() = runCatching {
        Timber.d("Disable all shortcuts.")
        ShortcutManagerCompat.disableShortcuts(
            context,
            listOf(
                QR_CODE_SCANNER_SHORTCUT_ID,
                CERTIFICATES_SHORTCUT_ID,
                CHECK_INS_SHORTCUT_ID,
                CONTACT_DIARY_SHORTCUT_ID
            ),
            null
        )
    }.onFailure { throwable ->
        Timber.e(throwable, "Failed to disable all Shortcuts")
    }

    private fun isCameraPermissionGranted() =
        CameraPermissionHelper.hasCameraPermission(context)

    private fun enableQrCodeScannerShortcut() =
        runCatching {
            Timber.d("Enable QrCodeScanner Shortcut")
            ShortcutManagerCompat.enableShortcuts(context, listOf(scannerShortcut))
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to enable QrCodeScanner Shortcut")
        }

    private fun disableQrCodeScannerShortcut() = runCatching {
        Timber.d("Disable QrCodeScanner Shortcut")
        ShortcutManagerCompat.disableShortcuts(context, listOf(QR_CODE_SCANNER_SHORTCUT_ID), null)
    }.onFailure { throwable ->
        Timber.e(throwable, "Failed to disable QrCodeScanner Shortcut")
    }

    private fun createShortcutIntent(shortcut: String) = Intent(context, LauncherActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra(SHORTCUT_EXTRA, shortcut)
    }

    companion object {
        private const val QR_CODE_SCANNER_SHORTCUT_ID = "scanner_id"
        private const val CERTIFICATES_SHORTCUT_ID = "certificates_id"
        private const val CHECK_INS_SHORTCUT_ID = "check_ins_id"
        private const val CONTACT_DIARY_SHORTCUT_ID = "contact_diary_id"
        const val SHORTCUT_EXTRA = "shortcut_extra"

        fun Intent.getShortcutExtra(): AppShortcuts? {
            getStringExtra(SHORTCUT_EXTRA)?.let {
                return AppShortcuts.valueOf(it)
            }
            return null
        }
    }
}
