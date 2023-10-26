package com.rummytitans.playcashrummyonline.cardgame.ui.more.module

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsHelper
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.api.APIInterface
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.ui.BaseViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.ConnectionDetector
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.google.gson.Gson
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalArgumentException
import javax.inject.Inject
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, val analyticsHelper: AnalyticsHelper
) : BaseViewModel<BaseNavigator>() {
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var myDialog: MyDialog? = null
    var feedbacks = arrayListOf("Bonus",
        "Gameplay",
        "Scoring",
        "Referral",
        "Transaction",
        "User Experience",
        "Verification & Account Related",
        "Withdrawal",
        "Other")
    var feedbackList = ObservableField<MutableList<String>>(feedbacks)
    var isLoading = ObservableBoolean(false)
    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)

    fun submit(title: String, mes: String, selectedItemId: Long) {

        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.SubmitFeedback,
                AnalyticsKey.Keys.Message to mes,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Feedback
            )
        )

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                isLoading.set(true)
                submit(title, mes, selectedItemId)
            }
            isLoading.set(false)
            return
        }
        isLoading.set(true)

        compositeDisposable.add(
            apiInterface.submitFeedback(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                title, mes, feedbacks[selectedItemId.toInt()]
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    isLoading.set(false)
                    navigator.showMessage(it.Response)
                }), ({
                    isLoading.set(false)
                    if (it is IllegalArgumentException)
                        navigator.showError(navigator.getStringResource(R.string.invalid_detail_error_msg))
                    else
                        navigator.handleError(it)

                }))
        )
    }
}
