package de.rki.coronawarnapp.util.shortcuts

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.launcher.LauncherActivity
import de.rki.coronawarnapp.util.AppShortcuts
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppShortcutsHelper @Inject constructor(@AppContext private val context: Context) {

    fun createAppShortcut() {
        val shortcut = ShortcutInfoCompat.Builder(context, CONTACT_DIARY_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setLongLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_contact_diary_shortcut_icon))
            .setIntent(createContactDiaryIntent())
            .build()

        ShortcutManagerCompat.addDynamicShortcuts(context, listOf(shortcut))
    }

    fun disableAppShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(CONTACT_DIARY_SHORTCUT_ID))
        }
    }

    private fun createContactDiaryIntent() = Intent(context, LauncherActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra(SHORTCUT_EXTRA_ID, AppShortcuts.CONTACT_DIARY.toString())
    }

    companion object {
        private const val CONTACT_DIARY_SHORTCUT_ID = "contact_diary_id"
        private const val SHORTCUT_EXTRA_ID = "shortcut_extra"

        fun getShortcutType(intent: Intent): AppShortcuts? {
            intent.getStringExtra(SHORTCUT_EXTRA_ID)?.let {
                return AppShortcuts.valueOf(it)
            }

            return null
        }
    }
}
