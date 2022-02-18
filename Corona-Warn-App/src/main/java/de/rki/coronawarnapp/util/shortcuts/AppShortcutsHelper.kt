package de.rki.coronawarnapp.util.shortcuts

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.ui.launcher.LauncherActivity
import de.rki.coronawarnapp.util.AppShortcuts
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppShortcutsHelper @Inject constructor(
    @AppContext private val context: Context,
    private val onboardingSettings: OnboardingSettings,
    private val coronaTestRepository: CoronaTestRepository
) {
    suspend fun restoreAppShortcut() = withContext(Dispatchers.IO) {
        // No shortcuts if test result is positive
        if (coronaTestRepository.allCoronaTests.first().any { it.isPositive }) {
            Timber.i("[AppShortcuts] Remove all shortcut items since exposure submission result is positive")
            removeAppShortcuts()
            return@withContext
        }

        // No shortcuts if not onboarded
        if (!onboardingSettings.isOnboarded) {
            Timber.i("[AppShortcuts] Remove all shortcut items since onboarding is not done yet")
            removeAppShortcuts()
            return@withContext
        }

        val shortcutScanner = ShortcutInfoCompat.Builder(context, QR_CODE_SCANNER_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_scanner_title))
            .setLongLabel(context.getString(R.string.app_shortcut_scanner_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_qr_code_scanner_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.QR_CODE_SCANNER.toString()))
            .setRank(0)
            .build()

        val shortcutCertificates = ShortcutInfoCompat.Builder(context, COVID_CERTIFICATES_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_certificates_title))
            .setLongLabel(context.getString(R.string.app_shortcut_certificates_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_certificates_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CERTIFICATES.toString()))
            .setRank(1)
            .build()

        val shortcutCheckIns = ShortcutInfoCompat.Builder(context, CHECK_INS_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_check_ins_title))
            .setLongLabel(context.getString(R.string.app_shortcut_check_ins_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_check_ins_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CHECK_INS.toString()))
            .setRank(2)
            .build()

        val shortcutDiary = ShortcutInfoCompat.Builder(context, CONTACT_DIARY_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setLongLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_contact_diary_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CONTACT_DIARY.toString()))
            .setRank(3)
            .build()

        val shortcutList = arrayListOf(
            shortcutDiary,
            shortcutCheckIns,
            shortcutCertificates
        )

        // don't show camera related actions if no camera access is granted
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            shortcutList.add(shortcutScanner)
        }

        for (shortcut in shortcutList) {
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        }
    }

    fun removeAppShortcuts() {
        ShortcutManagerCompat.removeDynamicShortcuts(
            context,
            listOf(
                QR_CODE_SCANNER_SHORTCUT_ID,
                COVID_CERTIFICATES_SHORTCUT_ID,
                CHECK_INS_SHORTCUT_ID,
                CONTACT_DIARY_SHORTCUT_ID
            )
        )
    }

    private fun createShortcutIntent(shortcut: String) = Intent(context, LauncherActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra(SHORTCUT_EXTRA, shortcut)
    }

    companion object {
        private const val QR_CODE_SCANNER_SHORTCUT_ID = "scanner_id"
        private const val COVID_CERTIFICATES_SHORTCUT_ID = "certificates_id"
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
