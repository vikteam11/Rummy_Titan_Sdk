package com.rummytitans.sdk.cardgame.ui.contectList

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.models.ContactModel
import com.rummytitans.sdk.cardgame.models.TempContactModel
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.ui.contectList.viewModel.ContactsViewModel
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyContectsBinding
import com.rummytitans.sdk.cardgame.databinding.DialogAlertNotificationRummyBinding
import com.rummytitans.sdk.cardgame.utils.utilClasses.createReferLink
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rummy_contects.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList


class ContactListActivity : BaseActivity() {


    lateinit var viewModel: ContactsViewModel


    lateinit var binding: ActivityRummyContectsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this
        ).get(ContactsViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_contects)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        viewModel.navigator = this
        viewModel.myDialog = MyDialog(this)
        binding.executePendingBindings()
        viewModel.getContactsApi()
        viewModel.referCode = intent?.getStringExtra("referCode") ?: ""
        hideKeyboard()

        contactList.adapter =
            ContactAdpater(
                viewModel.invitesCount,
                ArrayList(),
                viewModel.selectedContacts,
                viewModel.selectedColor
            )
        contactList.layoutManager = LinearLayoutManager(this)

        viewModel.mResponseData.observe(this, Observer {
            if (it.list.size != 0) {
                it.list.sortWith(Comparator { a, b -> a.name.compareTo(b.name) })
                contactList.adapter =
                    ContactAdpater(
                        viewModel.invitesCount, it.list,
                        viewModel.selectedContacts, viewModel.selectedColor
                    )
            } else {
                if (!viewModel.isAlertContactOpen) {
                    viewModel.isAlertContactOpen = true
                    showFetchContact(R.string.no_contact_pls_update)
                }
            }
        })

        viewModel.searchList.observe(this, Observer {
            setListToAdapter(it)
        })

        RxTextView.afterTextChangeEvents(binding.searchName).skipInitialValue()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { viewModel.filterList(it.editable().toString()) }


        binding.searchName.setOnEditorActionListener { _, actionId, event ->
            if ((event != null && (event.keyCode == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                sendMsg()
            }
            false
        }

        binding.refresh.setOnClickListener {
            showFetchContact()
            binding.searchName.setText("")
        }

    }

    private fun showFetchContact(msg: Int = -1) {
        val dialogView: DialogAlertNotificationRummyBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_alert_notification_rummy, null, false)
                ?: return

        dialogView.colorcode = viewModel.selectedColor.get()
        val mOfferDialog = MyDialog(this).getMyDialog(dialogView.root)
        mOfferDialog.setCancelable(false)

        mOfferDialog.show()
        if (msg == -1)
            dialogView.msg.text = getString(R.string.do_you_realy_wants_update_contacts)
        else dialogView.msg.text = getString(msg)
        dialogView.cancel.visibility = View.VISIBLE
        dialogView.cancel.setOnClickListener { mOfferDialog.dismiss() }
        dialogView.ok.text = getString(R.string.txt_sign_up_continue)
        dialogView.ok.setOnClickListener {
            checkPermissionState()
            mOfferDialog.dismiss()
            binding.searchName.setText("")
        }
    }

    private fun setListToAdapter(list: ArrayList<ContactModel>) {
        (contactList.adapter as ContactAdpater).let { it2 ->
            if (TextUtils.isEmpty(binding.searchName.text.toString())) {
                viewModel.mResponseData.value?.list?.let {
                    it2.mContactList = ArrayList(it)
                }
            } else it2.mContactList = ArrayList(list)

            it2.notifyDataSetChanged()
        }
    }

    fun sendMsg() {
        if (viewModel.selectedContacts.size != 0) {
            var contactNo = ""
            viewModel.selectedContacts.forEach {
                contactNo = contactNo + "" + it.mobile + ";"
            }

            if (TextUtils.isEmpty(viewModel.referCode)) {
                showError(R.string.please_wait_fetching_refer_code)
                return
            }
            if (TextUtils.isEmpty(viewModel.prefs.referUrl)) {
                createReferLink(viewModel.referCode) {
                    val referLink = it
                    if (it.isNotEmpty()) {
                        viewModel.prefs.referUrl = referLink
                        println("Firebase Dynamic Link ---> $referLink")
                        share(referLink, contactNo)
                    }
                }
            } else {
                share(viewModel.prefs.referUrl, contactNo)
            }
        } else {
            Toast.makeText(this, "Please select atleast 1 contact.", Toast.LENGTH_SHORT).show()
        }
    }


    fun share(url: String?, contactNo: String) {
        val shareContent =
            "Let's Battle is out in the Fantasy Sports Arena with MyTeam11. Download app from" +
                    "\n $url" +
                    "\n & use my refer code " + viewModel.referCode +
                    "\nto earn ₹100 Welcome Bonus. Join the #YehKhelHaiJunoonKa madness!"

        try {
            val smsIntent = Intent(Intent.ACTION_VIEW)
            smsIntent.putExtra("address", contactNo)
            smsIntent.putExtra("sms_body", shareContent)
            smsIntent.setData(Uri.parse("smsto:$contactNo"))
            smsIntent.setType("vnd.android-dir/mms-sms")
            startActivity(smsIntent)
            (contactList.adapter as ContactAdpater).clearSelected()
            viewModel.selectedContacts.clear()
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }


    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }



    @SuppressLint("Range")
    private fun fetchContacts() {
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
                        val name =
                            cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        //to get the contact names
                        email =
                            cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                    }
                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
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
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.saveContectsApi(it)
            }
    }

    private fun checkPermissionState() {
        val permission = Manifest.permission.READ_CONTACTS
        val res = checkCallingOrSelfPermission(permission)
        if (res == PackageManager.PERMISSION_GRANTED) {
            fetchContacts()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                123
            )
        }
    }

    private fun alertRequestContact() {
        val dialogView: DialogAlertNotificationRummyBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.dialog_alert_notification_rummy, null, false)
        dialogView.colorcode = viewModel.selectedColor.get()
        dialogView.msg.setText(getString(R.string.allow_contacts_msg))
        dialogView.discription.visibility = View.VISIBLE
        val mOfferDialog = MyDialog(this).getMyDialog(dialogView.root)
        mOfferDialog.setCancelable(false)
        mOfferDialog.show()

        dialogView.ok.setOnClickListener {
            mOfferDialog.dismiss()
        }
    }

    override fun onRequestPermissionsResult(RC: Int, per: Array<String>, PResult: IntArray) {
        when (RC) {
            123 -> if (PResult.isNotEmpty() && PResult[0] == PackageManager.PERMISSION_GRANTED)
                fetchContacts() else alertRequestContact()
        }
        super.onRequestPermissionsResult(RC, per, PResult)
    }

}