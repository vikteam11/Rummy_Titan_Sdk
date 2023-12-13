package com.rummytitans.sdk.cardgame.ui.refer

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.databinding.DialogAlertNotificationRummyBinding
import com.rummytitans.sdk.cardgame.databinding.FragmentReferRummyBinding
import com.rummytitans.sdk.cardgame.models.TempContactModel
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.contectList.ContactListActivity
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.ui.refer.adapter.ReferAdapter
import com.rummytitans.sdk.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.sdk.cardgame.ui.refer.viewmodel.ItemReferModel
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.utils.utilClasses.createReferLink
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FragmentRefer : BaseFragment(), MainNavigationFragment,
    ReferItemClick, BaseNavigator {

    lateinit var sheetBehavior: BottomSheetBehavior<*>

    companion object {
        fun newInstance(fromHome: Boolean = false,referCode:String=""): FragmentRefer {
            val obj = FragmentRefer()
            val bundle = Bundle()
            if (fromHome) bundle.putString(MyConstants.INTENT_PASS_COMING_FROM, "HOME")
            bundle.putString(MyConstants.INTENT_PASS_REFER_CODE, referCode)
            obj.arguments = bundle
            return obj
        }
    }

    lateinit var viewModel: ReferViewModel
    lateinit var binding: FragmentReferRummyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setLanguage()
        setTheme(inflater)
        viewModel =
            ViewModelProvider(this).get(ReferViewModel::class.java)
        binding = FragmentReferRummyBinding.inflate(localInflater ?: inflater, container, false).apply {
            lifecycleOwner = this@FragmentRefer
            viewmodel = this@FragmentRefer.viewModel
            viewModel.myDialog = MyDialog(requireActivity())
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navigator = this
        viewModel.isLoading.set(true)
        viewModel.showToolBar.set(
            arguments?.getString(MyConstants.INTENT_PASS_COMING_FROM)?.equals(
                "HOME"
            ) == true
        )
        arguments?.getString(MyConstants.INTENT_PASS_REFER_CODE)?.let { referCode->
            viewModel.referCode.set(referCode)
        }

        viewModel.fetchReferList()

        binding.imgUser.setImageResource(getAvtar(viewModel.prefs.avtarId ?: 1))
        binding.rvRefer.layoutManager = LinearLayoutManager(activity)
        binding.rvRefer.adapter = ReferAdapter(ArrayList(), this,viewModel.prefs.avtarId?:1)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.isLoading.set(false)
            viewModel.isSwipeLoading.set(true)
            viewModel.fetchReferList()
        }

        initClick()
        observeReferList()
        observeContacts()
    }

    private fun observeContacts() {
        viewModel.isContactSave.observe(viewLifecycleOwner, Observer {
            viewModel.prefs.IsContactSaved = true
            val intent = Intent(activity, ContactListActivity::class.java)
            intent.putExtra("referCode", viewModel.referCode.get())
            startActivity(intent)
        })
    }

    private fun observeReferList() {
        viewModel.listModel.observe(viewLifecycleOwner, Observer { list ->
            val tempList = ArrayList<ItemReferModel>()

            list.Response.forEach {
                tempList.add(
                    ItemReferModel(
                        it,
                        viewModel.selectedColor.get()!!
                    )
                )
            }

            if (list.Response.isNullOrEmpty()){
                if (activity is RummyMainActivity) {
                    (activity as? RummyMainActivity)?.replaceFragment(
                        FragmentShare.newInstance(viewModel.data.value)
                    )
                }
            }
            (binding.rvRefer.adapter as ReferAdapter).updateItems(tempList)
        })
    }

    private fun initClick(){
        binding.btnOption.setOnClickListener {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.ReferNewFriend,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.ReferAndEarn
                )
            )
            showBottomSheet()
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
                        requireActivity().shareReferMessage(
                            viewModel.prefs.referUrl,
                            viewModel.prefs.referShareMessage,
                            referCode.get())
                    }
                }else{
                    viewModel.sharingData.set(false)
                }
            }
        } else {
            println(viewModel.prefs.referUrl)
            viewModel.apply {
                requireActivity().shareReferMessage(viewModel.prefs.referUrl,referModel.get()?.referralShareMessage,referCode.get())
            }
        }
    }

    override fun onItemClick(position: Int) {
        showBottomSheet()
    }

    fun showBottomSheet() {
        startActivity(
            Intent(activity, ShareActivity::class.java)
                .putExtra("data", viewModel.data.value)
        )
    }

    override fun goBack() {
        activity?.onBackPressed()
    }

    override fun handleError(throwable: Throwable?) {
        throwable?.let {showError(throwable.message)  }
    }

    override fun showMessage(message: String?) {
        binding.swipeRefresh.isRefreshing = false
        if (TextUtils.isEmpty(message)) return
        showMessageView(message ?: "")
    }

    override fun showError(message: String?) {
        binding.swipeRefresh.isRefreshing = false
        if (TextUtils.isEmpty(message)) return
        showErrorMessageView(message ?: "")
    }

    override fun showError(message: Int) {
        binding.swipeRefresh.isRefreshing = false
        if (message == 0) return
        showErrorMessageView(message)
    }

    override fun logoutUser() {
        binding.swipeRefresh.isRefreshing = false
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

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

    private fun showAlert() {
        val dialogView: DialogAlertNotificationRummyBinding =
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
        dialogView.ok.setOnClickListener {
            mOfferDialog.dismiss()
        }
    }

    @SuppressLint("Range")
    private fun fetchContacts() {
        viewModel.contactLoading.set(true)
        val d = Observable.fromCallable {
            val mContectList = ArrayList<TempContactModel>()
            val cr = context?.contentResolver
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
                viewModel.contactLoading.set(false)
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
}