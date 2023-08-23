package com.rummytitans.playcashrummyonline.cardgame.ui.base

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.ui.deeplink.DeepLinkActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.playcashrummyonline.cardgame.widget.FadingSnackbar
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.CalendarContract
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.appsflyer.AppsFlyerLib
//import com.onesignal.OSInAppMessageAction
//import com.onesignal.OneSignal
import com.rummytitans.playcashrummyonline.cardgame.models.MatchModel
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import com.tapadoo.alerter.Alerter
import dagger.android.support.DaggerAppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity(), BaseNavigator {

    var snackbar: FadingSnackbar? = null

    var clearActivityStack = KillReceiver()

    var matchModel: MatchModel =
        MatchModel()

    var myTheme: Int = R.style.AppTheme_Safe

    val isTimesUpForQuiz = MutableLiveData<Boolean>()

    val calenderPermisions =
        arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
    val PERMISSION_REQUEST_CODE_CALENDER = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        AppsFlyerLib.getInstance().sendPushNotificationData(this)
       /* OneSignal.setInAppMessageClickHandler { osInAppMessageAction: OSInAppMessageAction? ->
            kotlin.runCatching {
                val clickAction = osInAppMessageAction?.clickName
                if (!TextUtils.isEmpty(clickAction))
                    startActivity(Intent(baseContext, DeepLinkActivity::class.java)
                        .putExtra("deepLink", clickAction))
            }
        }
        OneSignal.setNotificationOpenedHandler { callback->
           kotlin.runCatching {
               val deeplink=callback.notification.additionalData.getString("deeplink")?:""
               if (!TextUtils.isEmpty(deeplink))
                   startActivity(Intent(baseContext, DeepLinkActivity::class.java)
                       .putExtra("deepLink", deeplink))
           }
        }*/
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        registerReceiver(
            clearActivityStack,
            IntentFilter.create("clearStackActivity", "text/plain")
        )

    }



    fun hideKeyboardView() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window?.decorView?.windowToken, 0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    override fun onDestroy() {
        super.onDestroy()
        mDialogTimesUp?.dismiss()
        unregisterReceiver(clearActivityStack)
    }

    fun setSnackbarView(sbar: FadingSnackbar) {
        snackbar = sbar
    }

    //Using Timer
    open fun finishTimer(timerFinished: LiveData<Boolean>) {
        timerFinished.observe(this, Observer {
            if (it) {
                showDialogTimesUp()
            }
        })
    }

    //using Match Live Fantasy
    fun onTimesUpAlert() {
        showDialogTimesUp()
    }

    var mDialogTimesUp:Dialog?=null

    fun showDialogTimesUp() {
        val alertModel= AlertdialogModel(
            getString(R.string.dialog_times_up),
            getString(R.string.match_times_up_desc),
            positiveText = getString(R.string.dialog_times_up_go_back),
            onPositiveClick = {
                mDialogTimesUp?.dismiss()
                if (matchModel.MatchType == 7) {
                    isTimesUpForQuiz.value = true
                } else {
                    val intent = Intent("clearStackActivity")
                    intent.type = "text/plain"
                    sendBroadcast(intent)
                }
            },
            imgRes = R.drawable.ic_time_up_new)
        mDialogTimesUp = MyDialog(this).getAlertDialog(alertModel)
        mDialogTimesUp?.show()
    }

    override fun goBack() {
        hideKeyboardView()
        onBackPressed()
    }


    override fun handleError(throwable: Throwable?) {
        throwable?.let { showError(it.message) }
    }

    override fun showMessage(message: String?) {
        if (TextUtils.isEmpty(message)) return
        showMessageView(message ?: "")
    }

    override fun showError(message: String?) {
        if (TextUtils.isEmpty(message)) return
        showErrorMessageView(message ?: "")
    }

    override fun showError(message: Int) {
        if (message == 0) return
        showErrorMessageView(getString(message))
    }

    @CallSuper
    override fun logoutUser() {
        //HaptikSDK.logout(this)
        finishAffinity()
        startActivity(Intent(this, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) =
        if (resourseId == 0) "" else getString(resourseId)


    fun showErrorMessageView(message: String) {
        if (TextUtils.isEmpty(message)) return
        Alerter.create(this).enableVibration(false).setText(message)
            .setBackgroundColorRes(R.color.error_red).show()
    }

    fun showErrorMessageView(message: Int) {
        showErrorMessageView(getString(message))
    }

    fun showMessageView(message: String) {
        if (TextUtils.isEmpty(message)) return
        Alerter.create(this).enableVibration(false).setText(message)
            .setBackgroundColorRes(R.color.bluish_green).show()
    }

    fun addReminder(title: String, starttime: Long, endtime: Long, minutes: Int) {
        val eventUriString = "content://com.android.calendar/events"
        val eventValues = ContentValues()
        eventValues.put(CalendarContract.Events.CALENDAR_ID, 1)
        eventValues.put(CalendarContract.Events.TITLE, "MyTeam11 Quiz- $title")
        eventValues.put(CalendarContract.Events.DESCRIPTION, "MyTeam11 Quiz")
        eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, "India")
        eventValues.put(CalendarContract.Events.DTSTART, starttime)
        eventValues.put(CalendarContract.Events.DTEND, endtime)
        eventValues.put(CalendarContract.Events.HAS_ALARM, 1)
        val eventUri: Uri? = try {
            contentResolver?.insert(Uri.parse(eventUriString), eventValues)
        } catch (e: Exception) {
            null
        }
        val eventID: Long = eventUri?.lastPathSegment?.toLong() ?: 0L

        /***************** Event: Reminder(with alert) Adding reminder to event  */
        val reminderUriString = "content://com.android.calendar/reminders"
        val reminderValues = ContentValues()
        reminderValues.put("event_id", eventID)
        reminderValues.put("minutes", minutes)
        reminderValues.put("method", 1)
        try {
            contentResolver?.insert(Uri.parse(reminderUriString), reminderValues)
        } catch (e: Exception) {
        }
        showMessageView("Reminder Set Successfully")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun hasCalenderPermissions(): Boolean {
        for (perm in calenderPermisions)
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) return false
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestPermissionForCalender() {
        try {
            requestPermissions(calenderPermisions, PERMISSION_REQUEST_CODE_CALENDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun installApk(context: Context) {
        val stringBuilder = StringBuilder()
        stringBuilder.append(Environment.getExternalStorageDirectory().path)
        stringBuilder.append("/MyTeam11")
        val apkFileDirectory = File(stringBuilder.toString())
        apkFileDirectory.mkdir()

        val apkFile = File(apkFileDirectory, "myteam11.apk")
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val apkUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                apkFile
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(
                apkUri,
                "application/vnd.android.package-archive"
            )
            val resInfoList: List<ResolveInfo> =
                context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    apkUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class KillReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        (context as Activity).finish()
    }
}

class DownloadCompleteBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //MainApplication.forgroundActivityRef.get()?.showDialogOnComplete()
    }
}


