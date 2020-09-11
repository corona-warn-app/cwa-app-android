package de.rki.coronawarnapp.util.di

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import de.rki.coronawarnapp.CoronaWarnApplication
import timber.log.Timber

object Injector {
    lateinit var component: ApplicationComponent

    fun init(app: CoronaWarnApplication) {
        component = DaggerApplicationComponent.factory().create(app)
        component.inject(app)
    }

    fun setup(activity: Activity) {
        Timber.tag(TAG).d("Injecting %s", activity)

        // Using lifecycle callbacks would be even more awesome,
        // but Activity.onPreCreate isn't available for our minAPI
        AndroidInjection.inject(activity)

        if (activity is FragmentActivity) {
            activity.supportFragmentManager
                .registerFragmentLifecycleCallbacks(object :
                    FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentPreAttached(
                        fm: FragmentManager,
                        f: Fragment,
                        context: Context
                    ) {
                        if (f is AutoInject) {
                            Timber.tag(TAG).d("Injecting %s", f)
                            AndroidSupportInjection.inject(f)
                        }
                        super.onFragmentPreAttached(fm, f, context)
                    }
                }, true)
        }
    }

    private val TAG = Injector::class.java.simpleName
}
