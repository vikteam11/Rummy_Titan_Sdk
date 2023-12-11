package com.rummytitans.sdk.cardgame.ui.verifications.viewmodels

import android.text.TextUtils
import androidx.core.os.bundleOf
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsHelper
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.api.APIInterface
import com.rummytitans.sdk.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.sdk.cardgame.models.AddressKycContentModel
import com.rummytitans.sdk.cardgame.models.LoginResponse
import com.rummytitans.sdk.cardgame.models.VerificationOptionModel
import com.rummytitans.sdk.cardgame.ui.BaseViewModel
import com.rummytitans.sdk.cardgame.ui.verifications.AddressVerificationNavigator
import com.rummytitans.sdk.cardgame.ui.verifications.BottomSheetDataModel
import com.rummytitans.sdk.cardgame.utils.ConnectionDetector
import com.rummytitans.sdk.cardgame.utils.MyConstants
import com.rummytitans.sdk.cardgame.utils.bottomsheets.models.BottomSheetStatusDataModel
import com.rummytitans.sdk.cardgame.widget.MyDialog
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddressVerificationViewModel @Inject constructor(
    val prefs: SharedPreferenceStorageRummy,
    val gson: Gson,
    val apiInterface: APIInterface,
    val connectionDetector: ConnectionDetector, var analyticsHelper: AnalyticsHelper
) : BaseViewModel<AddressVerificationNavigator>() {
    val loginResponse: LoginResponse = gson.fromJson(prefs.loginResponse, LoginResponse::class.java)
    var myDialog: MyDialog? = null
    var isProgressLoading = ObservableBoolean(false)
    var url = ""
    var toolbarTitle = ObservableField("")
    var frontImgUrl = ObservableField("")
    var dlFrontImgUrl = ObservableField("")
    var backImgUrl = ObservableField("")
    var isOtherOptionHide = ObservableBoolean(true)

    var frontImgName = ObservableField("")
    var dlFrontImgName = ObservableField("")
    var backImgName = ObservableField("")
    var selectedDocName = ObservableField("Aadhaar Card")
    var isButtonEnabled = ObservableBoolean(false)
    var selectedDocSide = ObservableInt(MyConstants.DOC_SIDE_FRONT)
    var warningMessage = ObservableField("")
    val bottomSheetDataModel = ObservableField<BottomSheetDataModel>()

    var selectedState = ""
    var dlNumber:String = ""
    var aadhaarNumber:String = ""
    var dob:String = ""

    val regularColor = prefs.regularColor
    val safeColor = prefs.safeColor
    val selectedColor = ObservableField(if (prefs.onSafePlay) safeColor else regularColor)

    var colorTextArray:List<String>?=null
    var colorArray:List<Int>?=null
    var textSizeArray:List<Float>?= listOf(1.2f,1f)
    var fontArray:List<Int>?= null

    val selectedDocument = ObservableInt(1)
    val documentUploadStatus = ObservableBoolean(false)

    private val _kycNotes = MutableLiveData<AddressKycContentModel>()
    val kycNotes: LiveData<AddressKycContentModel>
        get() = _kycNotes

    fun setButtonEnabledOrDisabled(){
        isButtonEnabled.set(
            if(selectedDocument.get() == 1){
                !TextUtils.isEmpty(frontImgUrl.get()) &&  !TextUtils.isEmpty(backImgUrl.get())
             }else if(selectedDocument.get() == 2){
                dlNumber.length >= 10 && !TextUtils.isEmpty(dob)
             }else{
                aadhaarNumber.replace(" ","").let {AadhaarCardDigit->
                    AadhaarCardDigit.length == 12
                }
             }
        )
    }

    fun onSelectDocument(id:Int){
        selectedDocName.set(if(id ==1 )"Aadhaar Card" else "Driving License")
        selectedDocument.set(id)
        setButtonEnabledOrDisabled()
        navigatorAct.hideKeyboard()
    }

    fun showChooseDocumentSheet(docSide:Int){
        selectedDocSide.set(docSide)
        navigatorAct.showChooseUploadSheet(docSide)
    }

    fun updateKycData(kycNotes: AddressKycContentModel){
        _kycNotes.value = kycNotes
    }

    fun fetchAddressVerificationContent() {
        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                fetchAddressVerificationContent()
            }
            isProgressLoading.set(false)
            return
        }

        isProgressLoading.set(true)

        val api = getApiEndPointObject(prefs.appUrl2?:"")
        compositeDisposable.add(
            api.getKycContent(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                prefs.seletedLanguage?:"en"
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    isProgressLoading.set(false)
                    if (it.Status) {
                        warningMessage.set(it.Response?.RejectMessage)
                        _kycNotes.value = it.Response
                    }else{
                        navigator.showError(it.Message?:"")
                    }
                }, {
                    isProgressLoading.set(false)
                    navigator.handleError(it)
                })

        )
    }

    fun submitDetails(){
        if (selectedDocument.get()==1)
            submitAadhaarDetails()
        else
            submitDLDocuments()
    }

    private fun submitAadhaarDetails() {

        if (isProgressLoading.get())return
        if (!isValidAadhaarDetails()) return

        isProgressLoading.set(true)

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                submitAadhaarDetails()
            }
            isProgressLoading.set(false)
            return
        }
        navigatorAct.showUploadingSheet(
            BottomSheetStatusDataModel(
                loading = true
            )
        )

        isProgressLoading.set(true)
        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.AddressVerification,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification,
                AnalyticsKey.Keys.UploadType to "AadhaarCard"
            )
        )

        val imgMultiPartArray= arrayListOf<MultipartBody.Part>()
        if (selectedDocument.get()==MyConstants.SELECT_FILE_ADHAAR_CARD) {
            File(frontImgUrl.get().toString()).let {
                MultipartBody.Part.createFormData(
                    APIInterface.PAN_PHOTO + "\"; filename=front.jpg",
                    it.name,
                    it.asRequestBody("image/*".toMediaTypeOrNull())
                )
            }.apply {
                imgMultiPartArray.add(this)
            }

            if (!TextUtils.isEmpty(backImgUrl.get().toString())) {
                File(backImgUrl.get().toString()).let {
                    MultipartBody.Part.createFormData(
                        APIInterface.PAN_PHOTO + "\"; filename=back.jpg",
                        it.name,
                        it.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }.apply {
                    imgMultiPartArray.add(this)
                }
            }
        }


        compositeDisposable.add(
            apiInterface.uploadAddressProofFromAadhaar(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
                selectedDocument.get().toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                selectedState.toRequestBody("text/plain".toMediaTypeOrNull()),
                imgMultiPartArray)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    analyticsHelper.setUserProperty(
                        AnalyticsKey.Properties.ADDRESS_UPDATED,if(it.Status) "1" else "0"
                    )
                    documentUploadStatus.set(it.Status)

                    if (it.Status) {
                        loginResponse.IsFirstTime = true
                        prefs.loginResponse = gson.toJson(loginResponse)
                    }
                    isProgressLoading.set(false)
                    val code = if (it.Status) {
                        loginResponse.IsFirstTime = true
                        prefs.loginResponse = gson.toJson(loginResponse)
                        if(it.Response?.Status == true){
                            MyConstants.DOC_UPLOAD_SUCCESS
                        }else{
                            MyConstants.DOC_UPLOAD_FAILED
                        }
                    }else{
                        MyConstants.DOC_UPLOAD_FAILED
                    }
                    setUploadingModel(it.Status,it.Response?.Title?:"",it.Response?.Message?:"",code)
                }), ({
                    analyticsHelper.setUserProperty(
                        AnalyticsKey.Properties.ADDRESS_UPDATED, "0"
                    )
                    documentUploadStatus.set(false)
                    isProgressLoading.set(false)
                    setUploadingModel(false, statusCode = MyConstants.DOC_UPLOAD_FAILED)
                }))
        )
    }

    private fun submitDLDocuments() {

        if (isProgressLoading.get())return
        if (!isDlDocumentValid()) return

        isProgressLoading.set(true)

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                submitDLDocuments()
            }
            isProgressLoading.set(false)
            return
        }
        navigatorAct.showUploadingSheet(
            BottomSheetStatusDataModel(
                loading = true
            )
        )

        isProgressLoading.set(true)
        analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.AddressVerification,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification,
                AnalyticsKey.Keys.UploadType to "DrivingLicence"
            )
        )

        val customApiInterface=getApiEndPointObject(prefs.appUrl2?:"")

        val json = JsonObject()
        json.addProperty(APIInterface.UPLOAD_DOC_TYPE,2)
        json.addProperty(APIInterface.DOB,dob)
        json.addProperty(APIInterface.DL_NUMBER,dlNumber)
        compositeDisposable.add(
            customApiInterface.uploadAddressProofFromDL(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    analyticsHelper.setUserProperty(
                        AnalyticsKey.Properties.ADDRESS_UPDATED,if(it.Status) "1" else "0"
                    )
                    documentUploadStatus.set(it.Status)
                    if (it.Status) {
                        loginResponse.IsFirstTime = true
                        prefs.loginResponse = gson.toJson(loginResponse)
                    }
                    isProgressLoading.set(false)
                    setUploadingModel(it.Status,it.Response?.Title?:"",it.Response?.Message?:"",it.Response?.Status?:-1)
                }), ({
                    analyticsHelper.setUserProperty(AnalyticsKey.Properties.ADDRESS_UPDATED, "0")
                    documentUploadStatus.set(false)
                    isProgressLoading.set(false)
                    setUploadingModel(false)
                }))
        )
    }

    fun verifyWithAadhaarNumber() {

        if (isProgressLoading.get())return

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                verifyWithAadhaarNumber()
            }
            isProgressLoading.set(false)
            return
        }

        navigatorAct.showUploadingSheet(
            BottomSheetStatusDataModel(
                loading = true
            )
        )
        isProgressLoading.set(true)
        analyticsHelper.fireEvent(AnalyticsKey.Names.ButtonClick ,
            bundleOf(AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.AadhaarVerificationContinue,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.AddressVerification)
        )
        val json = JsonObject()
        json.addProperty("aadharNumber",aadhaarNumber.replace(" ",""))
        val customApiInterface=getApiEndPointObject(prefs.appUrl2?:"")

        compositeDisposable.add(
            customApiInterface.verifyWithAadhaar(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire,
            json)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    analyticsHelper.setUserProperty(
                        AnalyticsKey.Properties.ADDRESS_UPDATED,if(it.Status) "1" else "0"
                    )
                    documentUploadStatus.set(it.Status)
                    if (it.Status) {
                        loginResponse.IsFirstTime = true
                        prefs.loginResponse = gson.toJson(loginResponse)
                    }
                    isProgressLoading.set(false)

                    setUploadingModel(it.Status,it.Response?.Title?:"",it.Response?.Message?:"",it.Response?.Status ?:-1)
                }), ({
                    analyticsHelper.setUserProperty(
                        AnalyticsKey.Properties.ADDRESS_UPDATED, "0"
                    )
                    documentUploadStatus.set(false)
                    isProgressLoading.set(false)
                    setUploadingModel(false)
                }))
        )
    }

    fun initAadhaar() {

        if (isProgressLoading.get())return

        if (!connectionDetector.isConnected) {
            myDialog?.noInternetDialog {
                initAadhaar()
            }
            isProgressLoading.set(false)
            return
        }
        isProgressLoading.set(true)

        compositeDisposable.add(
            apiInterface.initAadhaar(
                loginResponse.UserId.toString(),
                loginResponse.ExpireToken,
                loginResponse.AuthExpire)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(({
                    if(it.Status){
                        navigatorAct.onAadhaarInitialized(it.Response?:"")
                    }else{
                        navigator.showError(it.Message?:"")
                    }
                    isProgressLoading.set(false)

                }), ({
                    isProgressLoading.set(false)
                    navigator.handleError(it)

                }))
        )
    }

    private fun setUploadingModel(status: Boolean,title:String="",message:String="",statusCode:Int=-1) {
        var animFile =0
        var imageFile = 0

        when(statusCode){
            MyConstants.DOC_UPLOAD_SUCCESS ->{
                animFile = R.raw.withdrawal_done_anim
            }
            MyConstants.DOC_UPLOAD_FAILED, MyConstants.DOC_UPLOAD_INVALID,
            MyConstants.DOC_UPLOAD_ALREADY_VERIFIED ->{
                animFile = R.raw.withdrawal_failed_anim
            }
            MyConstants.DOC_UPLOAD_AGE_BELOW_18->{
                imageFile = R.drawable.ic_ineligible
            }
            MyConstants.DOC_UPLOAD_RESTRICT->{
                imageFile = R.drawable.ic_restrict
            }else ->{
            // in case status code is undefined.
            animFile = if (status)
                R.raw.withdrawal_done_anim
            else
                R.raw.withdrawal_failed_anim
            imageFile = 0
        }
        }

        val responseTitle= when {
            title != "" -> title
            status -> navigator.getStringResource(R.string.document_uploaded)
            selectedDocument.get()==1 -> navigator.getStringResource(R.string.document_upload_failed)
            else -> navigator.getStringResource(R.string.address_verification_failed)
        }
        val responseDes= when {
            message != "" -> message
            status -> navigator.getStringResource(R.string.doc_success_description)
            selectedDocument.get()==1 -> navigator.getStringResource(R.string.doc_failed_description)
            else -> navigator.getStringResource(R.string.address_verification_failed_description)
        }

        navigatorAct.showUploadingSheet(
            BottomSheetStatusDataModel().also {
                it.title = responseTitle
                it.description =responseDes
                it.positiveButtonName = if (status)navigator.getStringResource(R.string.go_back) else navigator.getStringResource(R.string.try_again)
                it.loading = false
                it.isSuccess = status
                it.imageIcon = imageFile
                it.animationFileId = animFile
                it.showSuccessAnim=if (selectedDocument.get()==1) 0 else if(status)R.raw.success_blast_anim else 0
            }
        )
    }

    private fun isValidAadhaarDetails(): Boolean {
        if (TextUtils.isEmpty(frontImgUrl.get()) || TextUtils.isEmpty(backImgUrl.get())) {
            navigator.showError("Please select image to upload.")
            return false
        }
        return true
    }

    private fun isDlDocumentValid():Boolean{
        if (TextUtils.isEmpty(dlNumber)) {
            navigator.showError("Enter Driving License Number")
            return false
        }else if (TextUtils.isEmpty(dob)) {
            navigator.showError(navigator.getStringResource(R.string.select_dob))
            return false
        }
        return true
    }

    fun getVerifyOptions():ArrayList<VerificationOptionModel>{
        return kycNotes.value?.let { kycData->
            val documentOptions = arrayListOf<VerificationOptionModel>()

            if(kycData.IsOTPAadharAllow){
                documentOptions.add(
                    VerificationOptionModel(
                        navigator.getStringResource(R.string.varify_aadhar),
                        false,
                        R.drawable.img_digilocker,
                        0
                    )
                )
            }

            if(kycData.IsManualAadharAllow){
                documentOptions.add(
                    VerificationOptionModel(
                        navigator.getStringResource(R.string.upload_aadhar),
                        false,
                        R.drawable.img_aadhard_icon,
                        1
                    )
                )
            }
            if(kycData.IsManualDLAllow){
                documentOptions.add(
                    VerificationOptionModel(
                        navigator.getStringResource(R.string.upload_license),
                        false,
                        R.drawable.img_driving_icon,
                        2
                    )
                )
            }
            if(documentOptions.size == 1){
                documentOptions[0].isSelect = true
                isButtonEnabled.set(true)
            }
            documentOptions
        }?: kotlin.run {
            arrayListOf<VerificationOptionModel>()
        }
    }
}

