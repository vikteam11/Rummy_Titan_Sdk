package com.rummytitans.sdk.cardgame.ui.profile.info

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.models.LoginResponseRummy
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.home.MainNavigationFragment
import com.rummytitans.sdk.cardgame.ui.profile.info.viewmodel.ProfileInfoViewModel
import com.rummytitans.sdk.cardgame.ui.profile.updatePhone.UpdatePhoneActivity
import com.rummytitans.sdk.cardgame.utils.inTransaction
import com.rummytitans.sdk.cardgame.utils.validEmail
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.RummyTitanSDK
import com.rummytitans.sdk.cardgame.databinding.FragmentProfileInfoRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import kotlinx.android.synthetic.main.fragment_profile_info_rummy.*
import kotlinx.android.synthetic.main.fragment_profile_info_rummy.editDOB
import java.util.*

class FragmentProfileInfo : BaseFragment(),
    BaseNavigator, DBUpdateNavigortor {

    lateinit var profileInfoViewModel: ProfileInfoViewModel
    lateinit var binding: FragmentProfileInfoRummyBinding

    companion object {
        fun newInstance() = FragmentProfileInfo()
        const val UPDATE_PROFILE = 1221
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setLanguage()
        setTheme(inflater)
        profileInfoViewModel = ViewModelProvider(this)
            .get(ProfileInfoViewModel::class.java)
        binding =
            FragmentProfileInfoRummyBinding.inflate(localInflater ?: inflater, container, false).apply {
                lifecycleOwner = this@FragmentProfileInfo
                viewmodel = this@FragmentProfileInfo.profileInfoViewModel
                profileInfoViewModel.navigator = this@FragmentProfileInfo
                profileInfoViewModel.navigatorAct = this@FragmentProfileInfo
                profileInfoViewModel.myDialog = MyDialog(requireActivity())
            }

        binding.editUserName.addTextChangedListener {
            if (it.toString().isNotEmpty()) binding.inputLayoutUserName.error = null
            updateData()
        }

        binding.editDOB.addTextChangedListener {
            if (it.toString().isNotEmpty()) binding.inputLayoutDob.error = null
            updateData()
        }

        binding.editAddress.addTextChangedListener {
            if (it.toString().isNotEmpty()) binding.inputLayoutAddress.error = null
            updateData()
        }

        binding.edtState.addTextChangedListener {
            if (it.toString().isNotEmpty()) binding.inputLayoutState.error = null
            updateData()
        }

        binding.editPinCode.addTextChangedListener {
            if (it.toString().isNotEmpty())
                binding.inputLayoutPinCode.error = null
            updateData()
        }

        binding.rbGroup.setOnCheckedChangeListener { _, _ ->
            updateData()
        }

        val tf = ResourcesCompat.getFont(requireContext(), R.font.rubik_regular)
        binding.inputLayoutTeamName.typeface = tf
        binding.inputLayoutUserName.typeface = tf
        binding.inputLayoutDob.typeface = tf
        binding.inputLayoutAddress.typeface = tf
        binding.inputLayoutPinCode.typeface = tf
        binding.inputLayoutState.typeface = tf
        binding.inputLayoutEmail.typeface = tf
        binding.inputLayoutNumber.typeface = tf
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileInfoViewModel.isLoading.set(true)
        profileInfoViewModel.getState()
        binding.edtState.setOnClickListener { binding.spnState.performClick() }
        btnChangePassword.setOnClickListener { addFragment(FragmentChangePassword.newInstance()) }

        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireActivity(),R.style.RummySdkDatePickerDialogTheme, { _, p1, p2, p3 ->
                val dob = p3.toString() + "/" + (p2 + 1).toString() + "/" + p1.toString()
                editDOB.setText(dob)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        )
        val date = Date()
        date.year = date.year - 18
        datePickerDialog.datePicker.maxDate = date.time
        editDOB.setOnClickListener { datePickerDialog.show() }

        spnState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0)
                    profileInfoViewModel.selectedState = ""
                else {
                    profileInfoViewModel.selectedState = (view as TextView?)?.text as String? ?: ""
                    binding.edtState.setText(profileInfoViewModel.selectedState)
                }
            }
        }

        spnState.isLongClickable = false

        spnState.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val imm =
                    activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(spnState.windowToken, 0)
            }
            false
        }

        btnUpdateProfile.setOnClickListener {
            val gender = when {
                rbMale.isChecked -> "M"
                rbFemale.isChecked -> "F"
                rbOther.isChecked -> "O"
                else -> "M"
            }

            if (validation()) {
                profileInfoViewModel.updateProfile(
                    editUserName.text.toString(),
                    editDOB.text.toString(),
                    gender,
                    editAddress.text.toString(),
                    editPinCode.text.toString(),
                    editEmailAddress.text.toString(),
                            editPhone.text.toString()
                )
            }
        }

        swipeRefresh.setOnRefreshListener {
            profileInfoViewModel.isLoading.set(false)
            profileInfoViewModel.isSwipeLoading.set(true)
            profileInfoViewModel.getState()
        }
    }
    override fun updateProfileDataSuccess(successMessage: String) {
        showMessage(successMessage)
        Handler(Looper.getMainLooper()).postDelayed({
            requireActivity().finish()
        },1500)
    }

    private fun <F> addFragment(fragment: F) where F : Fragment, F : MainNavigationFragment {
        activity?.supportFragmentManager?.inTransaction {
            add(RummyMainActivity.FRAGMENT_ID, fragment).addToBackStack(null)
        }
    }

    override fun goBack() {
        activity?.onBackPressed()
    }


    override fun handleError(throwable: Throwable?) {
        showError(throwable?.message)
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
        showErrorMessageView(R.string.err_session_expired)
        RummyTitanSDK.rummyCallback?.logoutUser()
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

    override fun onEditMobileClick() {
        profileInfoViewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.MobileNoUpdated, bundleOf(
                AnalyticsKey.Keys.MobileNo to profileInfoViewModel.profileInfo.value?.Mobile,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.EditProfile
            )
        )

        activity?.let {
            startActivityForResult(Intent(it, UpdatePhoneActivity::class.java), UPDATE_PROFILE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_PROFILE && resultCode == Activity.RESULT_OK)
            profileInfoViewModel.fetchProfileData()
    }

    private fun validation(): Boolean {
        val email = editEmailAddress.text.toString()
        when {
            TextUtils.isEmpty(editUserName.text.toString()) -> {
                inputLayoutUserName.error = getStringResource(R.string.enter_your_name)
                return false
            }
            TextUtils.isEmpty(editDOB.text.toString()) -> {
                inputLayoutDob.error = getStringResource(R.string.enter_your_dob)
                return false
            }

            TextUtils.isEmpty(email)->{
                inputLayoutEmail.error = getStringResource(R.string.enter_your_address)
                return false
            }
            !validEmail(email) ->{
                inputLayoutEmail.error = getStringResource(R.string.err_invalid_email)
                return false
            }

            TextUtils.isEmpty(editAddress.text.toString()) -> {
                inputLayoutAddress.error = getStringResource(R.string.enter_your_address)
                return false
            }
            TextUtils.isEmpty(edtState.text.toString()) -> {
                inputLayoutState.error = getStringResource(R.string.select_your_state)
                return false
            }
          /*  TextUtils.isEmpty(editPinCode.text.toString()) -> {
                inputLayoutPinCode.error = getStringResource(R.string.enter_your_pin_code)
                return false
            }
            editPinCode.text.toString().length != 6 -> {
                inputLayoutPinCode.error = getStringResource(R.string.enter_valid_pin_code)
                return false
            }
            editPinCode.text.toString().startsWith("0") -> {
                inputLayoutPinCode.error = getStringResource(R.string.enter_valid_pin_code)
                return false
            }
            editPinCode.text.toString() == "000000" -> {
                inputLayoutPinCode.error = getStringResource(R.string.enter_valid_pin_code)
                return false
            }*/
            else -> return true
        }
    }

    private fun updateData() {
        val gender = when {
            rbMale.isChecked -> "M"
            rbFemale.isChecked -> "F"
            rbOther.isChecked -> "O"
            else -> "M"
        }
        val dobOld = profileInfoViewModel.profileInfo.value?.DOB

        val name = editUserName.text.toString()
        val dob = editDOB.text.toString()
        val address = editAddress.text.toString()
        val state = edtState.text.toString()
        val pinCode = editPinCode.text.toString()

        val update =
            gender != profileInfoViewModel.profileInfo.value?.Gender
                    || name != profileInfoViewModel.profileInfo.value?.Name
                    || dob != (if (!TextUtils.isEmpty(dobOld) && dobOld?.contains(" ") == true) dobOld.split(" ")[0] else dobOld)
                    || state != profileInfoViewModel.profileInfo.value?.StateName
                    || address != profileInfoViewModel.profileInfo.value?.Address
                    || pinCode != profileInfoViewModel.profileInfo.value?.PinCode
        profileInfoViewModel.updateData.set(update)

    }


}

interface DBUpdateNavigortor {
    fun updateProfileDataSuccess(successMessage: String){}
    fun saveToDB(loginResponse: LoginResponseRummy?) {}
    fun onEditMobileClick() {}
    fun onSuccesTeamUpdate(msg: String) {}
}