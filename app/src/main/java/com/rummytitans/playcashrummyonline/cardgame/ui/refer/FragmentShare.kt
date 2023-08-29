package com.rummytitans.playcashrummyonline.cardgame.ui.refer

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityRummyShareBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.DialogAlertNotificationRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.ReferModel
import com.rummytitans.playcashrummyonline.cardgame.models.TempContactModel
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.WebViewActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.playcashrummyonline.cardgame.ui.refer.viewmodel.ReferViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.*
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.playcashrummyonline.cardgame.ui.contectList.ContactListActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.refer.adapter.ReferContentAdapter
import com.rummytitans.playcashrummyonline.cardgame.utils.utilClasses.createReferLink
import com.rummytitans.playcashrummyonline.cardgame.utils.utilClasses.shareReferMessage
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FragmentShare : BaseFragment(), MainNavigationFragment,
    com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator {


   // @Inject
    //lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var binding: ActivityRummyShareBinding
    lateinit var viewModel: ReferViewModel
    private val contentAdapter: ReferContentAdapter by lazy {
        ReferContentAdapter(arrayListOf())
    }
    companion object {
        fun newInstance(model: ReferModel?, fromMainActivity: Boolean = false): FragmentShare {
            val obj = FragmentShare()
            val bundle = Bundle()
            bundle.putSerializable("data", model)
            bundle.putBoolean("fromMainActivity", fromMainActivity)
            obj.arguments = bundle
            return obj
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        setLanguage()
        setTheme(inflater)

        viewModel =
            ViewModelProvider(this).get(ReferViewModel::class.java)
        viewModel.navigator = this

        binding = ActivityRummyShareBinding.inflate(localInflater ?: inflater, container, false).apply {
            lifecycleOwner = this@FragmentShare
            viewModel = this@FragmentShare.viewModel
            icBack.visibility = View.GONE
            imgUser.visibility = View.GONE
            topBar.visibility = View.GONE
        }
        activity?.let { viewModel.myDialog = MyDialog(it) }

        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgUser.setImageResource(getAvtar(viewModel.prefs.avtarId ?: 1))

        initClick()
        observeContact()
        observeContents()
        viewModel.fetchReferContent()
        binding.rvContent.adapter = contentAdapter
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.ReferAndEarn
            )
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as? RummyMainActivity)?.showReferIcon(true)
        viewModel.sharingData.set(false)
    }

    private fun observeContact(){
        viewModel.isContactSave.observe(viewLifecycleOwner, Observer {
            viewModel.prefs.IsContactSaved = true
            val intent = Intent(activity, ContactListActivity::class.java)
            intent.putExtra("referCode", viewModel.referCode.get())
            startActivity(intent)
        })
    }

    fun observeContents(){
        viewModel.referContent.observe(viewLifecycleOwner) { referContent ->
            (activity as? RummyMainActivity)?.setReferCode(referContent.referCode?:"")
            contentAdapter.updateItems(referContent.content)
        }
    }

    private fun initClick() {
        binding.txtTermsAndCondition.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ReferTermsAndConditons,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.ReferAndEarn
                )
            )
            val url = if(viewModel.referContent.value?.tnCURL.isNullOrEmpty()){
                WebViewUrls.AppDefaultURL + WebViewUrls.SHORT_REFER_T_AND_C
            }else{
                viewModel.referContent.value?.tnCURL?:""
            }
            startActivity(
                Intent(activity, WebViewActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_WEB_URL, url)
                    .putExtra(MyConstants.INTENT_PASS_SHOW_CROSS, true)
                    .putExtra(
                        MyConstants.INTENT_PASS_WEB_TITLE,
                        getStringResource(R.string.refer_and_earn)
                    )
            )
        }

        binding.btnInviteContact.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.InviteContacts,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.ReferAndEarn
                )
            )
            viewModel.toggleBottomSheet()
            checkPermissionState()
        }

        binding.btnHowItWorks.setOnClickListenerDebounce {
            startActivity(
                Intent(requireContext(), WebViewActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_WEB_URL, WebViewUrls.AppDefaultURL+WebViewUrls.REFER_FRIEND)
                    .putExtra(MyConstants.INTENT_PASS_SHOW_CROSS, true)
                    .putExtra(MyConstants.INTENT_PASS_WEB_TITLE, getString(R.string.app_name_rummy))
            )
        }

        binding.btnShare.setOnClickListenerDebounce {
            if (!viewModel.isLoading.get()) {
                viewModel.toggleBottomSheet()
                showDialog()
                viewModel.analyticsHelper.fireEvent(
                    AnalyticsKey.Names.ButtonClick, bundleOf(
                        AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ShareRAndE,
                        AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.ReferAndEarn
                    )
                )
            }
        }
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
                        requireContext().shareReferMessage(referLink,referContent.value?.referralShareMessage,referCode.get())
                    }
                }else{
                    viewModel.sharingData.set(false)
                }
            }
        } else {
            println(viewModel.prefs.referUrl)
            viewModel.apply {
                requireContext().shareReferMessage(viewModel.prefs.referUrl,referContent.value?.referralShareMessage,referCode.get())
            }
        }
    }

    private fun showAlert() {
        var dialogView: DialogAlertNotificationRummyBinding? = null
        dialogView =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_alert_notification_rummy, null, false)
        dialogView.colorcode = viewModel.selectedColor.get()
        dialogView.msg.text = getString(R.string.allow_contacts_msg)
        dialogView.discription.visibility = View.VISIBLE
        val mOfferDialog = MyDialog(requireActivity()).getMyDialog(dialogView.root)
        mOfferDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mOfferDialog.show()

        dialogView.ok.setOnClickListener { mOfferDialog.dismiss() }
    }

    private fun checkPermissionState() {
        val permission = Manifest.permission.READ_CONTACTS
        val res = activity?.checkCallingOrSelfPermission(permission)
        if (res == PackageManager.PERMISSION_GRANTED) {
            if (!viewModel.prefs.IsContactSaved) {
                fetchContacts()
            } else {
                val intent = Intent(activity, ContactListActivity::class.java)
                intent.putExtra("referCode", viewModel.referCode.get())
                startActivity(intent)
            }
        } else {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    123
                )
            }
        }
    }

    @SuppressLint("Range")
    private fun fetchContacts() {
        viewModel.contactLoading.set(true)
        val d = Observable.fromCallable {
            val mContectList = ArrayList<TempContactModel>()
            val cr = activity?.contentResolver
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
                        val intent = Intent(activity, ContactListActivity::class.java)
                        intent.putExtra("referCode", viewModel.referCode.get())
                        startActivity(intent)
                    }
                } else showAlert()

        }
        super.onRequestPermissionsResult(RC, per, PResult)
    }

    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun handleError(throwable: Throwable?) {
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
        showErrorMessageView(message)
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

}