/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.rki.coronawarnapp.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.TaskStackBuilder;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavInflater;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.NavigatorProvider;

import java.lang.reflect.Method;
import java.util.ArrayDeque;

/**
 * Class used to construct deep links to a particular destination in a {@link NavGraph}.
 *
 * <p>When this deep link is triggered:
 * <ol>
 *     <li>The task is cleared.</li>
 *     <li>The destination and all of its parents will be on the back stack.</li>
 *     <li>Calling {@link NavController#navigateUp()} will navigate to the parent of the
 *     destination.</li>
 * </ol></p>
 * <p>
 * The parent of the destination is the {@link NavGraph#getStartDestination() start destination}
 * of the containing {@link NavGraph navigation graph}. In the cases where the destination is
 * the start destination of its containing navigation graph, the start destination of its
 * grandparent is used.
 * <p>
 * You can construct an instance directly with {@link #SafeNavDeepLinkBuilder(Context)} or build one
 * using an existing {@link NavController} via {@link NavController#createDeepLink()}.
 */
public final class SafeNavDeepLinkBuilder {
    private final Context mContext;
    private final Intent mIntent;

    private NavGraph mGraph;
    private int mDestId;
    private Bundle mArgs;

    static final String KEY_DEEP_LINK_IDS = "android-support-nav:controller:deepLinkIds";
    static final String KEY_DEEP_LINK_EXTRAS =
            "android-support-nav:controller:deepLinkExtras";

    /**
     * Construct a new SafeNavDeepLinkBuilder.
     * <p>
     * If the context passed in here is not an {@link Activity}, this method will use
     * {@link android.content.pm.PackageManager#getLaunchIntentForPackage(String)} as the
     * default activity to launch, if available.
     *
     * @param context Context used to create deep links
     * @see #setComponentName
     */
    public SafeNavDeepLinkBuilder(@NonNull Context context) {
        mContext = context;
        if (mContext instanceof Activity) {
            mIntent = new Intent(mContext, mContext.getClass());
        } else {
            Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(
                    mContext.getPackageName());
            mIntent = launchIntent != null ? launchIntent : new Intent();
        }
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    /**
     * @see NavController#createDeepLink()
     */
//    SafeNavDeepLinkBuilder(@NonNull NavController navController) {
//        this(navController.getContext());
//        mGraph = navController.getGraph();
//    }

    /**
     * Sets an explicit Activity to be started by the deep link created by this class.
     *
     * @param activityClass The Activity to start. This Activity should have a {@link NavController}
     *                      which uses the same {@link NavGraph} used to construct this
     *                      deep link.
     * @return this object for chaining
     */
    @NonNull
    public SafeNavDeepLinkBuilder setComponentName(@NonNull Class<? extends Activity> activityClass) {
        return setComponentName(new ComponentName(mContext, activityClass));
    }

    /**
     * Sets an explicit Activity to be started by the deep link created by this class.
     *
     * @param componentName The Activity to start. This Activity should have a {@link NavController}
     *                      which uses the same {@link NavGraph} used to construct this
     *                      deep link.
     * @return this object for chaining
     */
    @NonNull
    public SafeNavDeepLinkBuilder setComponentName(@NonNull ComponentName componentName) {
        mIntent.setComponent(componentName);
        return this;
    }

    /**
     * Sets the graph that contains the {@link #setDestination(int) deep link destination}.
     *
     * @param navGraphId ID of the {@link NavGraph} containing the deep link destination
     * @return this object for chaining
     */
    @NonNull
    public SafeNavDeepLinkBuilder setGraph(@NavigationRes int navGraphId) {
        return setGraph(new NavInflater(mContext, new PermissiveNavigatorProvider())
                .inflate(navGraphId));
    }

    /**
     * Sets the graph that contains the {@link #setDestination(int) deep link destination}.
     * <p>
     * If you do not have access to a {@link NavController}, you can create a
     * {@link NavigatorProvider} and use that to programmatically construct a navigation
     * graph or use {@link NavInflater#NavInflater(Context, NavigatorProvider) NavInflater}.
     *
     * @param navGraph The {@link NavGraph} containing the deep link destination
     * @return this object for chaining
     */
    @NonNull
    public SafeNavDeepLinkBuilder setGraph(@NonNull NavGraph navGraph) {
        mGraph = navGraph;
        if (mDestId != 0) {
            fillInIntent();
        }
        return this;
    }

    /**
     * Sets the destination id to deep link to.
     *
     * @param destId destination ID to deep link to.
     * @return this object for chaining
     */
    @NonNull
    public SafeNavDeepLinkBuilder setDestination(@IdRes int destId) {
        mDestId = destId;
        if (mGraph != null) {
            fillInIntent();
        }
        return this;
    }

    private void fillInIntent() {
        NavDestination node = null;
        ArrayDeque<NavDestination> possibleDestinations = new ArrayDeque<>();
        possibleDestinations.add(mGraph);
        while (!possibleDestinations.isEmpty() && node == null) {
            NavDestination destination = possibleDestinations.poll();
            if (destination.getId() == mDestId) {
                node = destination;
            } else if (destination instanceof NavGraph) {
                for (NavDestination child : (NavGraph) destination) {
                    possibleDestinations.add(child);
                }
            }
        }
        if (node == null) {
            final String dest = Integer.toString(mDestId);// NavDestination.getDisplayName(mContext, mDestId);
            throw new IllegalArgumentException("Navigation destination " + dest
                    + " cannot be found in the navigation graph " + mGraph);
        }
        try {
            Method buildDeepLinkIds = NavDestination.class.getDeclaredMethod("buildDeepLinkIds");
            buildDeepLinkIds.setAccessible(true);
            mIntent.putExtra(KEY_DEEP_LINK_IDS, (int[]) buildDeepLinkIds.invoke(node));
        } catch (Exception e) {
            throw new IllegalStateException("Can't access buildDeepLinkIds() method in the node", e);
        }

    }

    /**
     * Set optional arguments to send onto the destination
     *
     * @param args arguments to pass to the destination
     * @return this object for chaining
     */
    @NonNull
    public SafeNavDeepLinkBuilder setArguments(@Nullable Bundle args) {
        mArgs = args;
        mIntent.putExtra(KEY_DEEP_LINK_EXTRAS, args);
        return this;
    }

    /**
     * Construct the full {@link TaskStackBuilder task stack} needed to deep link to the given
     * destination.
     * <p>
     * You must have {@link #setGraph set a NavGraph} and {@link #setDestination set a destination}
     * before calling this method.
     * </p>
     *
     * @return a {@link TaskStackBuilder} which can be used to
     * {@link TaskStackBuilder#startActivities() send the deep link} or
     * {@link TaskStackBuilder#getPendingIntent(int, int) create a PendingIntent} to deep link to
     * the given destination.
     */
    @NonNull
    public TaskStackBuilder createTaskStackBuilder() {
        if (mIntent.getIntArrayExtra(KEY_DEEP_LINK_IDS) == null) {
            if (mGraph == null) {
                throw new IllegalStateException("You must call setGraph() "
                        + "before constructing the deep link");
            } else {
                throw new IllegalStateException("You must call setDestination() "
                        + "before constructing the deep link");
            }
        }
        // We create a copy of the Intent to ensure the Intent does not have itself
        // as an extra. This also prevents developers from modifying the internal Intent
        // via taskStackBuilder.editIntentAt()
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext)
                .addNextIntentWithParentStack(new Intent(mIntent));
        for (int index = 0; index < taskStackBuilder.getIntentCount(); index++) {
            // Attach the original Intent to each Activity so that they can know
            // they were constructed in response to a deep link
            taskStackBuilder.editIntentAt(index)
                    .putExtra(NavController.KEY_DEEP_LINK_INTENT, mIntent);
        }
        return taskStackBuilder;
    }

    /**
     * Construct a {@link PendingIntent} to the {@link #setDestination(int) deep link destination}.
     * <p>
     * This constructs the entire {@link #createTaskStackBuilder() task stack} needed.
     * <p>
     * You must have {@link #setGraph set a NavGraph} and {@link #setDestination set a destination}
     * before calling this method.
     * </p>
     *
     * @return a PendingIntent constructed with
     * {@link TaskStackBuilder#getPendingIntent(int, int)} to deep link to the
     * given destination
     */
    @NonNull
    public PendingIntent createPendingIntent() {
        int requestCode = 0;
        if (mArgs != null) {
            for (String key : mArgs.keySet()) {
                Object value = mArgs.get(key);
                requestCode = 31 * requestCode + (value != null ? value.hashCode() : 0);
            }
        }
        requestCode = 31 * requestCode + mDestId;
        return createTaskStackBuilder()
                .getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * A {@link NavigatorProvider} that only parses the basics: {@link NavGraph navigation graphs}
     * and {@link NavDestination destinations}, effectively only getting the base destination
     * information.
     */
    @SuppressWarnings("unchecked")
    private static class PermissiveNavigatorProvider extends NavigatorProvider {
        /**
         * A Navigator that only parses the {@link NavDestination} attributes.
         */
        private final Navigator<NavDestination> mDestNavigator = new Navigator<NavDestination>() {
            @NonNull
            @Override
            public NavDestination createDestination() {
                return new NavDestination("permissive");
            }

            @Nullable
            @Override
            public NavDestination navigate(@NonNull NavDestination destination,
                                           @Nullable Bundle args, @Nullable NavOptions navOptions,
                                           @Nullable Extras navigatorExtras) {
                throw new IllegalStateException("navigate is not supported");
            }

            @Override
            public boolean popBackStack() {
                throw new IllegalStateException("popBackStack is not supported");
            }
        };

        PermissiveNavigatorProvider() {
            addNavigator(new NavGraphNavigator(this));
        }

        @NonNull
        @Override
        public Navigator<? extends NavDestination> getNavigator(@NonNull String name) {
            try {
                return super.getNavigator(name);
            } catch (IllegalStateException e) {
                return mDestNavigator;
            }
        }
    }
}
