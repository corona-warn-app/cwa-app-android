package testhelpers

import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ScrollToAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.Matcher

fun recyclerScrollTo(
    position: Int? = null,
    additionalX: Int = 0,
    additionalY: Int = 0,
): ViewAction = RecyclerViewScrollAction(
    position = position,
    additionalX = additionalX,
    additionalY = additionalY
)

fun betterScrollTo(): ViewAction
{
    return ViewActions.actionWithAssertions(BetterScrollToAction())
}

// scroll-to action that also works with NestedScrollViews
class BetterScrollToAction:ViewAction by ScrollToAction()
{
    override fun getConstraints(): Matcher<View>
    {
        return allOf(
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
            isDescendantOfA(
                anyOf(
                    isAssignableFrom(ScrollView::class.java),
                    isAssignableFrom(HorizontalScrollView::class.java),
                    isAssignableFrom(NestedScrollView::class.java)
                )
            )
        )
    }
}

private class RecyclerViewScrollAction(
    private val position: Int? = null,
    private val additionalX: Int = 0,
    private val additionalY: Int = 0,
) : ViewAction {
    override fun getDescription(): String {
        return "scroll RecyclerView to bottom"
    }

    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as RecyclerView
        val itemCount = recyclerView.adapter?.itemCount
        val itemPosition = position ?: itemCount?.minus(1) ?: 0
        recyclerView.scrollToPosition(itemPosition)
        uiController.loopMainThreadUntilIdle()
        recyclerView.scrollBy(additionalX, additionalY)
        uiController.loopMainThreadUntilIdle()
    }
}

fun selectTabAtPosition(tabIndex: Int): ViewAction {
    return object : ViewAction {
        override fun getDescription() = "with tab at index $tabIndex"

        override fun getConstraints() = allOf(isDisplayed(), isAssignableFrom(TabLayout::class.java))

        override fun perform(uiController: UiController, view: View) {
            val tabLayout = view as TabLayout
            val tabAtIndex: TabLayout.Tab = tabLayout.getTabAt(tabIndex)
                ?: throw PerformException.Builder()
                    .withCause(Throwable("No tab at index $tabIndex"))
                    .build()

            tabAtIndex.select()
        }
    }
}

fun selectBottomNavTab(@IdRes id: Int): ViewAction {
    return object : ViewAction {
        override fun getDescription() = "with menu id $id"

        override fun getConstraints() = allOf(isDisplayed(), isAssignableFrom(BottomNavigationView::class.java))

        override fun perform(uiController: UiController, view: View) {
            val navigationView = view as BottomNavigationView
            navigationView.selectedItemId = id
        }
    }
}

fun setViewVisibility(value: Boolean): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(View::class.java)
    }

    override fun perform(uiController: UiController, view: View) {
        view.isVisible = value
    }

    override fun getDescription(): String {
        return "Show / Hide View"
    }
}
