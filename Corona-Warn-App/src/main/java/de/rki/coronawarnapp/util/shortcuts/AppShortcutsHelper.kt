package de.rki.coronawarnapp.util.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.launcher.LauncherActivity
import de.rki.coronawarnapp.util.AppShortcuts
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppShortcutsHelper @Inject constructor(@AppContext private val context: Context) {

    suspend fun restoreAppShortcut() = withContext(Dispatchers.IO) {
        val shortcutScanner = ShortcutInfoCompat.Builder(context, QR_CODE_SCANNER_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_scanner_title))
            .setLongLabel(context.getString(R.string.app_shortcut_scanner_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_qr_code_scanner_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.QR_CODE_SCANNER.toString()))
            .build()

        val shortcutCertificates = ShortcutInfoCompat.Builder(context, COVID_CERTIFICATES_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_certificates_title))
            .setLongLabel(context.getString(R.string.app_shortcut_certificates_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_certificates_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CERTIFICATES.toString()))
            .build()

        val shortcutCheckIns = ShortcutInfoCompat.Builder(context, CHECK_INS_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_check_ins_title))
            .setLongLabel(context.getString(R.string.app_shortcut_check_ins_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_check_ins_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CHECK_INS.toString()))
            .build()

        val shortcutDiary = ShortcutInfoCompat.Builder(context, CONTACT_DIARY_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setLongLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_contact_diary_shortcut_icon))
            .setIntent(createShortcutIntent(AppShortcuts.CONTACT_DIARY.toString()))
            .build()

        val shortcutList = listOf(
            shortcutDiary,
            shortcutCheckIns,
            shortcutCertificates,
            shortcutScanner
        )

        for (shortcut in shortcutList) {
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        }
    }

    fun removeAppShortcut() {
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
