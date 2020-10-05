package testhelpers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import de.rki.coronawarnapp.util.di.AutoInject
import timber.log.Timber
import javax.inject.Inject

class TestApplication : Application(), HasAndroidInjector {
    @Inject lateinit var androidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = androidInjector

    private lateinit var component: TestAppComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerTestAppComponent.factory().create(this)
        component.inject(this)
        setupActivityHook()
    }

    private fun setupActivityHook() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                setupFragmentHook(activity)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

            override fun onActivityStarted(activity: Activity) = Unit

            override fun onActivityResumed(activity: Activity) = Unit

            override fun onActivityPaused(activity: Activity) = Unit

            override fun onActivityStopped(activity: Activity) = Unit

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) = Unit

            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }

    private fun setupFragmentHook(activity: Activity) {
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
                            Timber.d("Injecting %s", f)
                            AndroidSupportInjection.inject(f)
                        }
                        super.onFragmentPreAttached(fm, f, context)
                    }
                }, true)
        }
    }
}
