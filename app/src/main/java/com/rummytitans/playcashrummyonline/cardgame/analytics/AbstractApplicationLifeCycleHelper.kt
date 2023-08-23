package com.rummytitans.playcashrummyonline.cardgame.analytics

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

abstract class AbstractApplicationLifeCycleHelper : Application.ActivityLifecycleCallbacks {

    private val LOG_TAG = AbstractApplicationLifeCycleHelper::class.java.simpleName
    private val ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF"
    private var inForeground = false

    /** Tracks the lifecycle of activities that have not stopped (including those restarted).  */
    private val activityLifecycleStateMap: WeakHashMap<Activity, String> = WeakHashMap()

    /**
     * Constructor. Registers to receive activity lifecycle events.
     * @param application The Android Application class.
     */
    open fun AbstractApplicationLifeCycleHelper(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        val screenOffReceiver = ScreenOffReceiver()
        application.registerReceiver(screenOffReceiver, IntentFilter(ACTION_SCREEN_OFF))
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        handleOnCreateOrOnStartToHandleApplicationEnteredForeground()
        activityLifecycleStateMap[activity] = "created"
    }

    override fun onActivityStarted(activity: Activity) {
        handleOnCreateOrOnStartToHandleApplicationEnteredForeground()
        activityLifecycleStateMap[activity] = "started"
    }

    override fun onActivityResumed(activity: Activity) {
        activityLifecycleStateMap[activity] = "resumed"
    }

    override fun onActivityPaused(activity: Activity) {
        activityLifecycleStateMap[activity] = "paused"
    }

    override fun onActivityStopped(activity: Activity) {
        // When the activity is stopped, we remove it from the lifecycle state map since we
        // no longer consider it keeping a session alive.
        activityLifecycleStateMap.remove(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

        if (activityLifecycleStateMap.containsKey(activity)) {
            activityLifecycleStateMap.remove(activity)
        }
    }

    /**
     * Call this method when your Application trims memory.
     * @param level the level passed through from Application.onTrimMemory().
     */
    open fun handleOnTrimMemory(level: Int) {
        // If no activities are running and the app has gone into the background.
        if (level >= Application.TRIM_MEMORY_UI_HIDDEN) {
            checkForApplicationEnteredBackground()
        }
    }


    /**
     * Called back when your application enters the Foreground.
     */
    protected abstract fun applicationEnteredForeground()

    /**
     * Called back when your application enters the Background.
     */
    protected abstract fun applicationEnteredBackground()

    /**
     * Called from onActivityCreated and onActivityStarted to handle when the application enters
     * the foreground.
     */
    private fun handleOnCreateOrOnStartToHandleApplicationEnteredForeground() {
        // if nothing is in the activity lifecycle map indicating that we are likely in the background, and the flag
        // indicates we are indeed in the background.
        if (activityLifecycleStateMap.size == 0 && !inForeground) {
            inForeground = true
            // Since this is called when an activity has started, we now know the app has entered the foreground.
            applicationEnteredForeground()
        }
    }

    open fun checkForApplicationEnteredBackground() {
        CoroutineScope(Dispatchers.Main).launch { // If the App is in the foreground and there are no longer any activities that have not been stopped.
            if (activityLifecycleStateMap.size == 0 && inForeground) {
                inForeground = false
                applicationEnteredBackground()
            }
        }
    }

    inner class ScreenOffReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkForApplicationEnteredBackground()
        }
    }
}