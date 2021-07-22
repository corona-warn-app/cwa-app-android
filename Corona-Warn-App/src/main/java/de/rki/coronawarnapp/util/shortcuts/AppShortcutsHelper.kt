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
        val shortcut = ShortcutInfoCompat.Builder(context, CONTACT_DIARY_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setLongLabel(context.getString(R.string.app_shortcut_contact_diary_title))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_contact_diary_shortcut_icon))
            .setIntent(createContactDiaryIntent())
            .build()

        if (ShortcutManagerCompat.getDynamicShortcuts(context).size == 0) {
            ShortcutManagerCompat.addDynamicShortcuts(context, listOf(shortcut))
        } else {
            ShortcutManagerCompat.updateShortcuts(context, listOf(shortcut))
        }
    }

    fun removeAppShortcut() {
        ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(CONTACT_DIARY_SHORTCUT_ID))
    }

    private fun createContactDiaryIntent() = Intent(context, LauncherActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra(SHORTCUT_EXTRA, AppShortcuts.CONTACT_DIARY.toString())
    }

    companion object {
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
