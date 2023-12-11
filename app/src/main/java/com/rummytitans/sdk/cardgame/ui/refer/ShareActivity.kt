package com.rummytitans.sdk.cardgame.ui.refer

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyShareBinding
import com.rummytitans.sdk.cardgame.databinding.DialogAlertNotificationRummyBinding
import com.rummytitans.sdk.cardgame.models.ReferModel
import com.rummytitans.sdk.cardgame.models.TempContactModel
import com.rummytitans.sdk.cardgame.ui.WebViewActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.ui.contectList.ContactListActivity
import com.rummytitans.sdk.cardgame.ui.refer.viewmodel.ReferViewModel
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.utils.utilClasses.shareReferMessage
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.utils.utilClasses.createReferLink
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rummy_share.*
import javax.inject.Inject

class ShareActivity : BaseActivity() {

    lateinit var binding: ActivityRummyShareBinding
    lateinit var viewModel: ReferViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =
            ViewModelProvider(this).get(ReferViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_share)
        binding.viewModel = viewModel
        viewModel.navigator = this
        viewModel.myDialog = MyDialog(this)

        (intent.getSerializableExtra("data") as? ReferModel)?.let {
            viewModel.referModel.set(it)
            viewModel.referCode.set(it.ReferCode)
        }

        txtTermsAndCondition.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ReferTermsAndConditons,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.ReferAndEarn
                )
            )
            val url =
                WebViewUrls.AppDefaultURL + WebViewUrls.SHORT_REFER_T_AND_C
            startActivity(
                Intent(this, WebViewActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_WEB_URL, url)
                    .putExtra(
                        MyConstants.INTENT_PASS_WEB_TITLE,
                        getStringResource(R.string.refer_and_earn)
                    )
            )
        }

        btnInviteContact.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.InviteContacts,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.ReferAndEarn
                )
            )
            viewModel.toggleBottomSheet()
            checkPermissionState()
        }

        btnHowItWorks.setOnClickListenerDebounce {
            startActivity(
                Intent(this, WebViewActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_WEB_URL, WebViewUrls.AppDefaultURL+WebViewUrls.REFER_FRIEND)
                    .putExtra(MyConstants.INTENT_PASS_SHOW_CROSS, true)
                    .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, getString(R.string.app_name_rummy))
            )
        }

        btnShare.setOnClickListenerDebounce {
            if (!viewModel.isLoading.get()) {
                viewModel.analyticsHelper.fireEvent(
                    AnalyticsKey.Names.ButtonClick, bundleOf(
                        AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ShareRAndE,
                        AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.ReferAndEarn
                    )
                )
                viewModel.toggleBottomSheet()
                showDialog()
            }
        }

        viewModel.isContactSave.observe(this, Observer {
            viewModel.prefs.IsContactSaved = true
            val intent = Intent(this, ContactListActivity::class.java)
            intent.putExtra("referCode", viewModel.referCode.get())
            startActivity(intent)
        })

        binding.executePendingBindings()
    }

    override fun onResume() {
        super.onResume()
        viewModel.sharingData.set(false)
    }

    private fun showDialog() {

        if (TextUtils.isEmpty(viewModel.referCode.get())) {
            showError(R.string.please_wait_fetching_refer_code)
            return
        }
        viewModel.sharingData.set(true)
        if (TextUtils.isEmpty(viewModel.prefs.referUrl)) {
            createReferLink(viewModel.referCode.get()) {
                val referLink = it
                if (it.isNotEmpty()) {
                    viewModel.prefs.referUrl = referLink
                    println("Firebase Dynamic Link ---> $referLink")
                    viewModel.apply {
                        shareReferMessage(viewModel.prefs.referUrl,referModel.get()?.referralShareMessage,referCode.get())
                    }
                }else{
                    viewModel.sharingData.set(false)
                }
            }
        } else {
            println(viewModel.prefs.referUrl)
            viewModel.apply {
                shareReferMessage(viewModel.prefs.referUrl,referModel.get()?.referralShareMessage,referCode.get())
            }
        }
    }

    private fun showAlert() {
        val dialogView: DialogAlertNotificationRummyBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_alert_notification_rummy, null, false)
        dialogView.colorcode = viewModel.selectedColor.get()
        dialogView.msg.text = getString(R.string.allow_contacts_msg)
        dialogView.discription.visibility = View.VISIBLE
        val mOfferDialog = MyDialog(this).getMyDialog(dialogView.root)
        mOfferDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mOfferDialog.show()
        dialogView.ok.setOnClickListener { mOfferDialog.dismiss() }
    }

    fun checkPermissionState() {
        val permission = Manifest.permission.READ_CONTACTS
        val res = this.checkCallingOrSelfPermission(permission)
        if (res == PackageManager.PERMISSION_GRANTED) {
            if (!viewModel.prefs.IsContactSaved) {
                fetchContacts()
            } else {
                val intent = Intent(this, ContactListActivity::class.java)
                intent.putExtra("referCode", viewModel.referCode.get())
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_CONTACTS), 123
            )
        }
    }

    @SuppressLint("Range")
    fun fetchContacts() {
        viewModel.isLoading.set(true)
        val d = Observable.fromCallable {
            val mContectList = ArrayList<TempContactModel>()
            val cr = contentResolver
            val cur = cr?.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

            if (cur?.count ?: 0 > 0) {
                while (cur != null && cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                    val name =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )
                    var email = ""
                    while (cur1?.moveToNext() == true) {
                        //to get the contact email
                        email =
                            cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                    }
                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id), null
                        )

                        while (pCur!!.moveToNext()) {
                            val phoneNo =
                                pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            mContectList.add(
                                TempContactModel(
                                    name,
                                    phoneNo,
                                    email
                                )
                            )
                        }
                        pCur.close()
                    }
                }
            }
            cur?.close()
            return@fromCallable mContectList
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.saveContactsApi(it)
            }
    }

    override fun onRequestPermissionsResult(RC: Int, per: Array<String>, PResult: IntArray) {
        when (RC) {
            123 ->
                if (PResult.isNotEmpty() && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!viewModel.prefs.IsContactSaved) {
                        fetchContacts()
                    } else {
                        val intent = Intent(this, ContactListActivity::class.java)
                        intent.putExtra("referCode", viewModel.referCode.get())
                        startActivity(intent)
                    }
                } else showAlert()

        }
        super.onRequestPermissionsResult(RC, per, PResult)
    }

}