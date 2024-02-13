package com.rummytitans.sdk.cardgame.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.*
import android.util.Base64
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.github.florent37.viewtooltip.ViewTooltip
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import com.jakewharton.rxbinding2.widget.RxTextView
import com.jakewharton.rxbinding2.widget.TextViewAfterTextChangeEvent
import com.rummytitans.sdk.cardgame.BuildConfig
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.databinding.BottomSheetDialogAlertWarningsBinding
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.NewPaymentGateWayModel
import com.rummytitans.sdk.cardgame.ui.WebViewActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.deeplink.DeepLinkActivityRummy
import com.rummytitans.sdk.cardgame.ui.verifications.RummySDKAddressVerificationActivity
import com.rummytitans.sdk.cardgame.utils.MyConstants.DATE_TYPE
import com.rummytitans.sdk.cardgame.utils.MyConstants.HOURS_TYPE
import com.rummytitans.sdk.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.sdk.cardgame.utils.bottomsheets.BottomSheetDialogBinding
import com.rummytitans.sdk.cardgame.utils.bottomsheets.LottieBottomSheetDialog
import com.rummytitans.sdk.cardgame.utils.bottomsheets.WebViewBottomSheetDialog
import com.rummytitans.sdk.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.net.NetworkInterface
import java.sql.Timestamp
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.Pair
import kotlin.math.roundToInt


fun SharedPreferenceStorageRummy.getAPPURL() =
    if (TextUtils.isEmpty(appUrl)) "https://app.myteam11.com" else appUrl

private val PANCARD_PATTERN = Pattern.compile("[A-Z,a-z]{5}[0-9]{4}[A-Z,a-z]{1}")
private val AADHAAR_PATTERN = Pattern.compile("^[2-9]{1}[0-9]{3}[0-9]{4}[0-9]{4}$")
private val UPI_PATTERN = Pattern.compile("[a-zA-Z0-9_]{3,}@[a-zA-Z]{3,}")

inline fun <reified VM : ViewModel> AppCompatActivity.viewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(this.viewModelStore, provider).get(VM::class.java)

inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

fun getWebUrls(url: String): String {
    return WebViewUrls.AppDefaultURL  + url
}

fun View.setOnClickListenerDebounce(debounceTime: Long = 600L, action: (v: View) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action(v)
            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

fun sendToCloseAbleInternalBrowser(activity: Context, webUrl: String,title:String="") {
    Log.e("url>>>>>>>>>>>>> ",webUrl)
    if (TextUtils.isEmpty(webUrl) || !URLUtil.isValidUrl(webUrl)) return
    activity.startActivity(
        Intent(activity, WebViewActivity::class.java)
            .putExtra(MyConstants.INTENT_PASS_WEB_URL, webUrl)
            .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, if(title =="") activity.getString(R.string.app_name_rummy) else title)
            .putExtra(MyConstants.INTENT_PASS_SHOW_TOOLBAR, true)
            .putExtra(MyConstants.INTENT_PASS_SHOW_CROSS, true)
    )
}


fun Window.transparentStatusBar(){
    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = Color.TRANSPARENT
}

fun getPlatformBasedWebViewUrl(url: String):String{
    return WebViewUrls.AppDefaultURL+ url +if (BuildConfig.isPlayStoreApk==1) "?platform=1" else ""
}

fun getDisplayMetrics(context: Context?):Int {
    val displayMetrics = DisplayMetrics()
    val windowManager = context?.getSystemService(Context.WINDOW_SERVICE)
            as WindowManager
    windowManager.defaultDisplay.getMetrics(displayMetrics)
     return displayMetrics.widthPixels
}

fun checkAppAvailabity(context: Context?,packageName: String):Boolean {
    return runCatching {
        val isAvailableInDevice = context?.packageManager?.getPackageInfo(
            packageName, PackageManager.GET_ACTIVITIES
        )?.applicationInfo?.enabled
        Log.e("testingAppsAvailable", "$isAvailableInDevice")
    }.isSuccess
}

fun validEmail(charSequence: CharSequence) = Patterns.EMAIL_ADDRESS.matcher(charSequence).matches()


fun validAadhaar(charSequence: String) = AADHAAR_PATTERN.matcher(charSequence).matches()

fun validEmailTestCase(charSequence: CharSequence): Boolean {
    val regex =
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    val pattern = Pattern.compile(regex)
    return pattern.matcher(charSequence).matches()
}

fun convertIntToDp(mSpace: Int, view: View) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, mSpace.toFloat(), view.resources.displayMetrics
).toInt()

fun validPassword(target: CharSequence) = !TextUtils.isEmpty(target) && target.length >= 6

fun validPasswordTestCases(target: String) = !isEmptyString(target) && target.length >= 6

fun passwordPolicy(password: String): Boolean {
    return !TextUtils.isEmpty(password) && password.matches(Regex("(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&*!])(?=\\S+$).{8,}"))
}

fun passwordPolicyTestCases(password: String): Boolean {
    return !isEmptyString(password) && password.matches(Regex("(?=.*[0-9])(?=.*[a-z])(?=.*[@#$%^&*!])(?=\\S+$).{8,}"))
}

fun passwordDigit(password: String): Boolean {
    return !TextUtils.isEmpty(password) && password.matches(Regex(".*[0-9].*"))
}

fun passwordDigitTestCase(password: String): Boolean {
    return !isEmptyString(password) && password.matches(Regex(".*[0-9].*"))
}

fun passwordAlphabate(password: String): Boolean {
    return !TextUtils.isEmpty(password) && password.matches(Regex(".*[a-z].*"))
}

fun passwordAlphabateTestCases(password: String): Boolean {
    return !isEmptyString(password) && password.matches(Regex(".*[a-z].*"))
}

fun passwordSpecialSymbol(password: String): Boolean {
    return !TextUtils.isEmpty(password) && password.matches(Regex(".*[@#\$%^&!?].*"))
}

fun passwordSpecialSymbolTestCases(password: String): Boolean {
    return !isEmptyString(password) && password.matches(Regex(".*[@#\$%^&!?].*"))
}

fun passwordLength(password: String) = !TextUtils.isEmpty(password) && password.length > 7

fun passwordLengthTestCases(password: String) = !isEmptyString(password) && password.length > 7

fun isEmptyString(text: String?) = text == null || text.isEmpty()

fun validPinCode(number: String) = !isEmptyString(number) && number.length == 6

fun validTeamNameTestCase(text: String) =
    !isEmptyString(text) && text.length > 3 && text.length < 25


fun getAvtar(avtarId: Int): Int {
    var avtar = R.drawable.avatar_1
    when (avtarId) {
        MyConstants.DefaultAvatarID -> avtar = R.drawable.dummy_avtar
        1 -> avtar = R.drawable.avatar_1
        2 -> avtar = R.drawable.avatar_2
        3 -> avtar = R.drawable.avatar_3
        4 -> avtar = R.drawable.avatar_4
        5 -> avtar = R.drawable.avatar_5
        6 -> avtar = R.drawable.avatar_7
        7 -> avtar = R.drawable.avatar_8
        8 -> avtar = R.drawable.avatar_9
        9 -> avtar = R.drawable.avatar_10
        10 -> avtar = R.drawable.avatar_11
        11 -> avtar = R.drawable.avatar_12
        12 -> avtar = R.drawable.avatar_13
        13 -> avtar = R.drawable.avatar_14
        14 -> avtar = R.drawable.avatar_15
        15 -> avtar = R.drawable.avatar_16
        16 -> avtar = R.drawable.avatar_17
        17 -> avtar = R.drawable.avatar_18
        18 -> avtar = R.drawable.avatar_19
        19 -> avtar = R.drawable.avatar_20
        20 -> avtar = R.drawable.avatar_21
        21 -> avtar = R.drawable.avatar_22
        22 -> avtar = R.drawable.avatar_23
        23 -> avtar = R.drawable.avatar_24
        24 -> avtar = R.drawable.avatar_25
        25 -> avtar = R.drawable.avatar_26
        26 -> avtar = R.drawable.avatar_26
    }
    return avtar
}
fun validRedemCodeTestCases(code: String) = !isEmptyString(code) && code.length > 4

fun validContestCodeTestCases(code: String) = !isEmptyString(code) && code.length > 4

fun validReferCode(target: CharSequence) =
    if (!TextUtils.isEmpty(target)) target.length >= 6 else true

fun validReferCodeTestCase(target: String) =
    if (!isEmptyString(target)) target.length >= 6 else true

fun validMobile(target: CharSequence): Boolean {
    return !TextUtils.isEmpty(target) && target.length == 10 && !target.startsWith("0")
}

fun validMobileTestCases(target: CharSequence): Boolean {
    return !isEmptyString(target.toString()) && target.length == 10
}

fun isMobileNumber(target: CharSequence) = target.matches("[0-9]+".toRegex())

fun validOTP(target: CharSequence): Boolean {
    return !TextUtils.isEmpty(target) && target.length == 6 && TextUtils.isDigitsOnly(target)
}

fun validOTPTestCases(target: CharSequence): Boolean {
    return !isEmptyString(target.toString()) && target.length == 6 && target.matches(Regex("[0-9]+"))
}

fun convertDpToPixel(dp: Float, context: Context): Int {
    return (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

fun convertDpToPixel(dp: Int, context: Context): Int {
    return (dp * (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT))
}

fun validPancard(number: CharSequence) = PANCARD_PATTERN.matcher(number).matches()

fun validUpi(number: CharSequence) = UPI_PATTERN.matcher(number).matches()

fun validAmount(amount: String): Boolean {
    return if (!isEmptyString(amount)) (amount.getOrNull(0)?.equals('.') == false)
    else false
}

fun validIfscCode(code: String): Boolean {
    return !TextUtils.isEmpty(code) && code.length == 11
}

fun validIfscCodetestCases(code: String): Boolean {
    return !isEmptyString(code) && code.length == 11
}

fun checkAndSetLanguage(context: Context) {
    if (TextUtils.isEmpty(LocaleHelper.getLanguage(context))) LocaleHelper.setLocale(context)
    else LocaleHelper.onAttach(context)
}

fun String.hasNumber() = this.matches("[0-9]+".toRegex())

fun Int.toBoolean() = this != 0

fun Boolean.toInt() = if (this) 1 else 0

fun Int.toPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.toDp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()

fun Int.toDpFloat(context: Context): Float = (this / context.resources.displayMetrics.density)



fun String.toMatchStatus(): String {
    return when {
        this == MyConstants.MATCH_NOT_STARTED -> "Fixture"
        this == MyConstants.MATCH_STARTED -> "Live"
        this == MyConstants.MATCH_COMPLETED -> "Completed"
        else -> ""
    }
}


fun String.toIntWithCheck() = if (TextUtils.isEmpty(this)) 0 else try {
    Integer.parseInt(this)
} catch (e: Exception) {
    0
}

fun EditText.toIntWithCheck() = if (TextUtils.isEmpty(this.text.toString())) 0 else try {
    Integer.parseInt(this.text.toString())
} catch (e: Exception) {
    0
}

fun setTimeByType(time: String, timeType: Int): String {
    var returnText = ""
    val formater = SimpleDateFormat("dd-MM-yyyy H:m:s")
    val date = formater.parse(time)
    val stamp = Timestamp(date.time)
    val d = Date(stamp.time)

    if (timeType == DATE_TYPE) {
        val s = SimpleDateFormat("d MMM hh:mm a")
        returnText = s.format(d)
    } else if (timeType == HOURS_TYPE) {
        val timerCount = formater.parse(time).time - MyConstants.CURRENT_TIME.value!!
        val hours = (TimeUnit.MILLISECONDS.toHours(timerCount))
        returnText = "$hours Hours Left"
    }
    return returnText
}

fun getNotLoginUser() = LoginResponse()
    .apply {
    UserId = 0
    AuthExpire = "0"
    ExpireToken = "0"
    Name = ""
}


fun sendToExternalBrowser(activity: Activity, url: String) {
    if (TextUtils.isEmpty(url) || !URLUtil.isValidUrl(url)) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    activity.startActivity(intent)
}


fun getCardImage(card_type: String): Pair<Int, Boolean> {
    return when (card_type) {
        "maestro" -> Pair(R.drawable.card_maestro, true)
        "visa" -> Pair(R.drawable.card_visa, false)
        "master" -> Pair(R.drawable.card_master, false)
        "diner" -> Pair(R.drawable.card_dinersclub, false)
        "jcb" -> Pair(R.drawable.card_jcb, false)
        "discover" -> Pair(R.drawable.card_discover, false)
        "amex" -> Pair(R.drawable.card_amex, false)
        "rupay" -> Pair(R.drawable.card_rupay, false)
        else -> Pair(R.drawable.card_unknown, false)
    }
}


/*Show cross icon on toolBaar*/



fun sendToPlayStore(activity: Activity, packageName: String) {
    if (TextUtils.isEmpty(packageName)) return
    activity.startActivity(
        Intent(
            Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.playstore_url) + packageName)
        )
    )
}

fun Activity.redirectToPlayStore(packageName: String){
    packageManager?.let {
        kotlin.runCatching {
            val isAvailableInDevice = it.getPackageInfo(
                packageName, PackageManager.GET_ACTIVITIES
            ).applicationInfo.enabled
            if (isAvailableInDevice) {
                sendToExternalApp(this, it, packageName)
            } else {
                sendToPlayStore(this, packageName)
            }
        }.onFailure {
            sendToPlayStore(this, packageName)
        }
    }
}

fun sendToExternalApp(activity: Activity, packageManager: PackageManager?, packageName: String) {
    val intent: Intent? = packageManager?.getLaunchIntentForPackage(packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    activity.startActivity(intent)
}

fun isValidReferCode(target: String) = !TextUtils.isEmpty(target) && target.length >= 6

fun adjustAlpha(color: Int, factor: Float): Int {
    val alpha = (Color.alpha(color) * factor).roundToInt()
    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)
    return Color.argb(alpha, red, green, blue)
}



fun showToolTip(activity: Activity?, view: View, message: String,position: ViewTooltip.Position=ViewTooltip.Position.BOTTOM,delay:Long=1000L) {
    activity?.let {
        ViewTooltip.on(it, view).autoHide(true, delay).corner(30)
            .color(ContextCompat.getColor(it, R.color.text_color1))
            .position(position).text(message).withShadow(false)
            .textSize(TypedValue.COMPLEX_UNIT_SP, 12.0f).show()
    }
}


fun <S> getArrayList(array: Array<S>): ArrayList<S> {
    val arraylist = ArrayList<S>()
    arraylist.addAll(array)
    return arrayListOf()
}

fun getSearchObservable(view: TextView): Observable<TextViewAfterTextChangeEvent> =
    RxTextView.afterTextChangeEvents(view).skipInitialValue()
        .debounce(500, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

inline fun <reified T : BaseFragment> getCurrentFragment(position: Int = -1, viewPager: ViewPager) =
    ((viewPager.adapter as? ViewPagerAdapter)?.arrFragments
        ?.elementAtOrNull(if (position == -1) viewPager.currentItem else position) as? T)


fun Context.deleteApp(packageName: String) {
    kotlin.runCatching {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }.onFailure { print(it.message) }
}

fun Context.openDeeplink(url: String,fromGame:Boolean=false) {
    if (TextUtils.isEmpty(url)) return
   /* val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.putExtra(MyConstants.INTENT_COME_FROM_GAME,fromGame)
    startActivity(intent)
*/
    startDeeplinkActivity(url)
}

fun Context.startDeeplinkActivity(url: String){
    startActivity(
        Intent(this, DeepLinkActivityRummy::class.java).putExtra("deepLink", url)
    )
}

fun Context.isAppLanguageEnglish() =
    LocaleHelper.getLanguage(this) == getString(R.string.english_code)

fun String.toBase64() = try {
    Base64.encodeToString(toByteArray(), Base64.DEFAULT) ?: ""
} catch (e: Exception) {
    ""
}

fun Context.copyCode(code: String?) {
    if (TextUtils.isEmpty(code)) return
    val clip = ClipData.newPlainText("code", code)
    val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
    manager?.setPrimaryClip(clip)
}

fun JSONObject.toBundle(): Bundle {
    val bundle = Bundle()
    for (key in keys()) {
        val value = get(key)
        when (value) {
            is String -> bundle.putString(key, value)
            is Int -> bundle.putInt(key, value.toString().toInt())
            is Double -> bundle.putDouble(key, value.toString().toDouble())
            is Long -> bundle.putLong(key, value.toString().toLong())
            is Float -> bundle.putFloat(key, value.toString().toFloat())
            is Boolean -> bundle.putBoolean(key, value.toString().toBoolean())
        }
    }
    return bundle
}

fun SharedPreferenceStorageRummy.toUserDetail(gson: Gson): LoginResponse? {
    if (loginResponse.isNullOrEmpty() || !loginCompleted) return null
    return gson.fromJson(loginResponse, LoginResponse::class.java)
}

fun Context.shareUrl(url:String?,message: String?){
    if (TextUtils.isEmpty(url))  return
    ShareCompat.IntentBuilder(this)
        .setType("text/plain")
        .setChooserTitle("Share By")
        .setText("$message $url")
        .startChooser()
}
fun Context.getColorInt(id:Int)= ContextCompat.getColor(this,id)

fun ImageView.setTint(colorRes: Int) {
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(context.getColorInt(colorRes)))
}
fun TextView.changeTextColor(colorRes: Int) {
    setTextColor(context.getColorInt(colorRes))
}

fun Activity.changeStatusBarColor(view: View,color: Int){
    window?.statusBarColor = ContextCompat.getColor(this, color)
    window?.let {
        WindowCompat.getInsetsController(it, view)?.isAppearanceLightStatusBars =
            false
    }
}

fun View.setDrag(flag:Boolean){
    val behavior = BottomSheetBehavior.from(this)
    behavior.isDraggable = flag
}

fun Activity.transparentStatusBar(){
    setWindowFlag( WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
    window.decorView.systemUiVisibility =  View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
    window.statusBarColor = Color.TRANSPARENT
}

fun Activity.drawOverStatusBar(){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}

fun Activity.setWindowFlag( bits: Int, on: Boolean) {
    val winParams: WindowManager.LayoutParams = window.attributes
    if (on) {
        winParams.flags = winParams.flags or bits
    } else {
        winParams.flags = winParams.flags and bits.inv()
    }
    window.attributes = winParams
}

fun EditText.setFocus(flag: Boolean){
    isFocusableInTouchMode = flag
    isCursorVisible = flag
    if(flag)
        requestFocus()
    else{
        clearFocus()
    }
}



fun getIpAddress(): String {
    try {
        val useIPv4=true
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            for (addr in Collections.list(intf.inetAddresses)) {
                if (!addr.isLoopbackAddress) {
                    var ipAddr: String = addr.hostAddress
                    val isIPv4 = ipAddr.indexOf(':') < 0

                    if (isIPv4 && !useIPv4) {
                        continue
                    }
                    if (useIPv4 && !isIPv4) {
                        val delim = ipAddr.indexOf('%') // drop ip6 zone suffix
                        ipAddr = if (delim < 0) ipAddr.uppercase() else ipAddr.substring(0, delim)
                            .uppercase()
                    }
                    return ipAddr
                }
            }
        }
    } catch (e: Exception) {
    }
    return ""
}

fun Activity.hasPermissions(permissions: Array<String>): Boolean{
    for (permission in permissions){
        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
            return false
        }
    }
    return true
}

fun Activity.launchAddressVerificationScreen(verificationRejectMessage:String,fromDeepLink:Boolean=false){
    val intent = Intent(
        this,
        RummySDKAddressVerificationActivity::class.java)
        .putExtra("FROM_SPLASH", false)
        .putExtra(MyConstants.INTENT_PASS_VERIFICATION_REJECT_MSG,verificationRejectMessage)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK  or Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivityForResult(
        intent
        ,MyConstants.REQUEST_UPDATE_VERIFY_DETAILS)
}

fun Fragment.launchAddressVerificationScreen(verificationRejectMessage:String){
    val intent = Intent(requireContext(), RummySDKAddressVerificationActivity::class.java)
        .putExtra("FROM_SPLASH", false)
        .putExtra(MyConstants.INTENT_PASS_VERIFICATION_REJECT_MSG,verificationRejectMessage)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK  or Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivityForResult(
        intent
        , MyConstants.REQUEST_UPDATE_VERIFY_DETAILS)
}
fun sendToInternalBrowser(activity: Context, webUrl: String,title: String="") {
    if (TextUtils.isEmpty(webUrl) || !URLUtil.isValidUrl(webUrl)) return
    activity.startActivity(
        Intent(activity, WebViewActivity::class.java)
            .putExtra(MyConstants.INTENT_PASS_WEB_URL, webUrl)
            .putExtra(MyConstants.INTENT_PASS_WEB_TITLE,
                if(title=="")activity.getString(R.string.app_name_rummy) else title)
    )
}

fun hideKeyboard(view: View,context: Activity) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}


fun setAddMoreGatewayItem(gateway: NewPaymentGateWayModel.GatewayList?) {
    when(gateway?.Type?:0){
        1 ->{
            gateway?.AddMoreTitle="All Wallets"
            gateway?.AddMoreDesc="Paytm, PhonePe, Amazon Pay & MORE"
            gateway?.AddMoreIcon= R.drawable.ic_allwalets
        }
        2 ->{
            gateway?.AddMoreTitle="View All Net Banking"
            gateway?.AddMoreDesc="Pay directly from your bank"
            gateway?.AddMoreIcon= R.drawable.ic_bank_new
        }
        3 ->{
            gateway?.AddMoreTitle="Add New Card"
            gateway?.AddMoreDesc="Save & pay via cards"
            gateway?.AddMoreIcon= R.drawable.ic_add_new
        }
        4 ->{
            gateway?.AddMoreTitle="Pay With UPI ID"
            gateway?.AddMoreDesc="You need to have a valid UPI ID"
            gateway?.AddMoreIcon= R.drawable.ic_add_new
        }
    }
}




fun Activity.showRestrictLocationDialog(msg:String){
    val alertDialog = LottieBottomSheetDialog(
        this, BottomSheetStatusDataModel().apply {
            title =getString(R.string.restricted_location_found)
            description = getString(R.string.restricted_location_msg)
            positiveButtonName= getString(R.string.dialog_times_up_go_back)
            imageIcon = R.drawable.ic_restrict
            cancelAble = true
        },
    )
    alertDialog.show()

    alertDialog.setOnDismissListener {
        finish()
    }
}
fun Context.showBottomSheetWebView(
    url: String,
    color:String,
    title:String=getString(R.string.app_name_rummy)
){
    WebViewBottomSheetDialog(
        context = this,
        webUrl = url,
        color = color,
        title = title
    ).show()
}


fun Context.showBottomSheetTNC(
    url: String,
    title:String,
    desc:String,
    hyperLink:String
){
    BottomSheetDialogBinding<BottomSheetDialogAlertWarningsBinding>(
        this,
        R.layout.bottom_sheet_dialog_alert_warnings
    ).apply {
        binding.imgCross.setOnClickListener {
            dismiss()
        }
        binding.alertModel = AlertdialogModel(
            title = title,
            description = desc,
            showClose = true,
            url = url,
            hyperLink = hyperLink,
            onPositiveClick = {
                sendToCloseAbleInternalBrowser(context,url,title)
            }
        )
        binding.executePendingBindings()
        show()
    }
}

fun Double.decimalFormat():Double{
    val format =  DecimalFormat("##.##").format(this)
    return format.toDouble()
}


fun String.isMyTeamDeeplink():Boolean{
    return this.contains("myteam11storedeeplink:",true) || this.contains("myTeamDeepLink:",true)
}
fun Double.formatInString():String =
    DecimalFormat("##.##").format(this)

