package com.rummytitans.sdk.cardgame.ui.profile

import android.content.DialogInterface.OnKeyListener
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.databinding.ActivityProfileRummyBinding
import com.rummytitans.sdk.cardgame.databinding.RummyDialogUpdateTeamNameBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseActivity
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.sdk.cardgame.ui.profile.info.DBUpdateNavigortor
import com.rummytitans.sdk.cardgame.ui.profile.info.viewmodel.ProfileInfoViewModel
import com.rummytitans.sdk.cardgame.ui.profile.updatePhone.UpdatePhoneActivity
import com.rummytitans.sdk.cardgame.ui.verify.viewmodel.VerifyViewModel
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.utils.alertDialog.AlertdialogModel
import com.rummytitans.sdk.cardgame.utils.bottomsheets.BottomSheetAlertDialog
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.rummytitans.sdk.cardgame.databinding.RummyBottomsheetUpdateAvtarDialogBinding
import com.rummytitans.sdk.cardgame.ui.profile.avtaar.AvtarNavigator
import com.rummytitans.sdk.cardgame.ui.profile.avtaar.ProfileAvatarListAdapter
import com.rummytitans.sdk.cardgame.ui.profile.avtaar.ProfileAvtarItem
import com.rummytitans.sdk.cardgame.ui.profile.avtaar.ProfileAvtarModel
import com.rummytitans.sdk.cardgame.ui.verify.adapter.ProfileVerifyItemAdapter
import com.rummytitans.sdk.cardgame.utils.bottomsheets.BottomSheetDialogBinding
import kotlinx.android.synthetic.main.rummy_dialog_update_team_name.*


class ProfileActivity : BaseActivity(), ProfileSelectListener, DBUpdateNavigortor,AvtarNavigator {

    companion object {
        val UPDATE_PROFILE = 12221
    }

    lateinit var profileViewModel: ProfileViewModel
    lateinit var profileInfoViewModel: ProfileInfoViewModel
    lateinit var verificationViewModel: VerifyViewModel
    lateinit var binding: ActivityProfileRummyBinding

    var avatarListAdapter: ProfileAvatarListAdapter? = null

    private val REQUSET_CODE_VERIFY_DETAIL = 102
    private val REQUSET_CODE_EDIT_PROFILE = 101
    private var currentChipID = -1
    private var mTeamUpdateBinding: RummyDialogUpdateTeamNameBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        drawOverStatusBar()
        super.onCreate(savedInstanceState)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        verificationViewModel = ViewModelProvider(this).get(VerifyViewModel::class.java)
        profileInfoViewModel = ViewModelProvider(this).get(ProfileInfoViewModel::class.java)
        profileInfoViewModel.navigator = this
        verificationViewModel.navigator = this
        profileViewModel.navigator = this
        profileInfoViewModel.navigatorAct = this

        binding = DataBindingUtil.setContentView(this,R.layout.activity_profile_rummy)
        binding.lifecycleOwner = this
        binding.profileViewModel = profileViewModel
        binding.verificationViewModel = verificationViewModel
        binding.profileInfoViewModel = profileInfoViewModel

        initClick()
        observerVerificationItem()
        observeProfileData()
        verificationViewModel.fetchVerificationData()
        profileInfoViewModel.fetchProfileData()

        profileViewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Profile,
            )
        )
    }

    private fun observeProfileData(){
        profileInfoViewModel.data.observe(this, Observer {
            binding.profileInfoViewModel = profileInfoViewModel
            profileInfoViewModel.prefs.avtarId = it.AvtarId
            profileInfoViewModel.prefs.userName = it.Name
            profileInfoViewModel.prefs.userTeamName = it.TeamNmae
            profileInfoViewModel.prefs.refertext = it.REferAmount

            updateAvtar(it.AvtarId)

            binding.icUser.setImageResource(getAvtar(it.AvtarId))
        })
    }

    private fun observerVerificationItem() {
        verificationViewModel.verificationInfo.observe(this){
            val list = verificationViewModel.getVerificationItems(true)
            binding.rvVerification.layoutManager = GridLayoutManager(this,list.size)
            binding.rvVerification.adapter = ProfileVerifyItemAdapter(verificationViewModel.getVerificationItems(true),
                true)
        }
    }

    private fun initClick(){
        bottomSheetListener()
        binding.icUser.setOnClickListenerDebounce {
            showAvatarBottomSheet()
        }
        binding.imageView28.setOnClickListenerDebounce {
            binding.icUser.performClick()
        }
        binding.btnLogout.setOnClickListenerDebounce {
            BottomSheetAlertDialog(
                this, AlertdialogModel(
                    getString(R.string.app_name),
                    getString(R.string.log_out_device_msg),
                    getString(R.string.no),
                    getString(R.string.yes),
                    onPositiveClick = {

                        profileViewModel.logoutUser()
                    }
                )
            ).show()
        }

        binding.changePhonno.setOnClickListener {
            onEditMobileClick()
        }

        binding.txtVerifyAccount.setOnClickListener {

            verificationViewModel.verificationInfo.value?.let {
                val step = when {
                    !it.EmailVerify -> AnalyticsKey.Values.VerificationStepEmail
                    !it.PanVerify -> AnalyticsKey.Values.VerificationStepPan
                    !it.BankVerify -> AnalyticsKey.Values.VerificationStepBank
                    else -> "AllDone"
                }
                verificationViewModel.analyticsHelper.fireEvent(
                    AnalyticsKey.Names.ButtonClick, bundleOf(
                        AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.UpdateVerificationDetails,
                        AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Profile,
                        AnalyticsKey.Keys.Step to step
                    )
                )
            }

            startActivityForResult(
                Intent(this, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "verify"),
                REQUSET_CODE_VERIFY_DETAIL
            )
        }
        binding.btnUpdateTeamName.setOnClickListener {
            updateProfileTeamName(it)
        }

        binding.editProfile.setOnClickListener {
            verificationViewModel.analyticsHelper.apply {
                addTrigger(AnalyticsKey.Screens.Profile,AnalyticsKey.Values.UpdateDetails)
                fireEvent(
                    AnalyticsKey.Names.ButtonClick, bundleOf(
                        AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.EditProfile,
                        AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Profile
                    )
                )
            }

            startActivityForResult(
                Intent(this, CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "editprofile"),
                REQUSET_CODE_EDIT_PROFILE
            )
        }
    }

    override fun onEditMobileClick() {
        verificationViewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to AnalyticsKey.Names.MobileNoUpdated,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Profile,
                )
        )
        startActivityForResult(
            Intent(this, UpdatePhoneActivity::class.java), UPDATE_PROFILE
        )
    }

    override fun getStringResource(resourseId: Int): String {
        return getString(resourseId)
    }

    override fun setAvatarPosition(pos: Int) {
        if (pos == -1) return
        profileViewModel.selectedAvatar.set(pos + 1)
    }

    private fun updateAvtar(avtarId: Int) {
        profileViewModel.selectedAvatar.set(avtarId)
        avatarListAdapter?.setSelected(avtarId)
    }

    override fun onSuccesTeamUpdate(msg: String) {
      binding.txtTeamName.text = msg
    }

    private fun getAvtarList(): ArrayList<ProfileAvtarModel> {
        val avatars = ArrayList<ProfileAvtarItem>()
        val newAvatars = ArrayList<ProfileAvtarItem>()
        val imageArray = resources.obtainTypedArray(R.array.rummy_avatar_image)
        for (i in 0 until imageArray.length()) {
            val item = ProfileAvtarItem(
                imageArray.getResourceId(i, 0),
                avtarId =  i+1,
                selected = false
            )
            if(i < 9){
                avatars.add(item)
            }else{
                newAvatars.add(item)
            }
        }
        val avatarsList = ArrayList<ProfileAvtarModel>()
        avatarsList.add(ProfileAvtarModel(title="", avtarList = avatars))
        avatarsList.add(ProfileAvtarModel(title="New Avatars", avtarList = newAvatars))
        return  avatarsList
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUSET_CODE_EDIT_PROFILE) {
            profileInfoViewModel.fetchProfileData()
        } else if (requestCode == REQUSET_CODE_VERIFY_DETAIL) {
            verificationViewModel.fetchVerificationData()
        } else if (requestCode == UPDATE_PROFILE) {
            profileInfoViewModel.fetchProfileData()
        }
    }

    fun updateProfileTeamName(v: View) {
        profileInfoViewModel.teamName.set("")
        mTeamUpdateBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.rummy_dialog_update_team_name, null, false)
        mTeamUpdateBinding?.apply {
            viewModel = profileInfoViewModel

            editTeamName.addTextChangedListener {
                inputTeamName.error = null
                inputTeamName.isErrorEnabled = it.toString().isNotEmpty()
                profileInfoViewModel.checkTeamName(it.toString())
                txtNameHint.visibility = if(it.toString().isNotEmpty()) View.GONE else View.VISIBLE
            }

            MyDialog(this@ProfileActivity).getFullScreenDialog(root).apply {
                btnCancel.setOnClickListener {
                    resetTeamUpdateDataParams()
                    dismiss()
                }
                btnUpdateTeamNameNow.setOnClickListener {
                    if (TextUtils.isEmpty(profileInfoViewModel.teamName.get())) {
                        mTeamUpdateBinding?.inputTeamName?.error =
                            getString(R.string.please_enter_team_name)
                        return@setOnClickListener
                    } else if (!profileInfoViewModel.isValidTeamName.get()) {
                        mTeamUpdateBinding?.inputTeamName?.error =
                            getString(R.string.please_enter_team_name)
                        return@setOnClickListener
                    }
                    profileInfoViewModel.saveTeamName()
                    resetTeamUpdateDataParams()
                    dismiss()
                }
            }.show()

            with(profileInfoViewModel) {
                isTeamAvailable.observe(this@ProfileActivity, Observer {
                    inputTeamName.apply {
                        if (!TextUtils.isEmpty(teamName.get())) {
                            val (msg, color) = if (it)
                                Pair(R.string.txt_team_name_correct, R.color.rummy_maingreen)
                            else
                                Pair(R.string.team_name_not_available, R.color.error_code)
                            val trueColor = ContextCompat.getColor(this@ProfileActivity, color)
                            val myList =
                                ColorStateList(arrayOf(intArrayOf()), intArrayOf(trueColor))
                            hintTextColor = myList
                            boxStrokeColor = trueColor
                            setErrorTextColor(myList)
                            inputTeamName.error = getString(msg)
                        }
                    }
                })
            }
        }
    }

    private fun resetTeamUpdateDataParams() {
        profileInfoViewModel.isSuggestionAvailable.set(false)
        profileInfoViewModel.isValidTeamName.set(false)
        profileInfoViewModel.teamName.set("")
    }

    override fun onSelectAvtar(id: Int) {
        profileViewModel.selectedAvatar.set(id)
    }

    private var avatarDialog:BottomSheetDialogBinding<RummyBottomsheetUpdateAvtarDialogBinding>? = null

    fun showAvatarBottomSheet(){
        avatarDialog?.show()
    }
    private fun bottomSheetListener(){
        avatarDialog =  BottomSheetDialogBinding<RummyBottomsheetUpdateAvtarDialogBinding>(
            this,
            R.layout.rummy_bottomsheet_update_avtar_dialog
        ).apply {
            binding.imgCross.setOnClickListener {
                avatarDialog?.behavior?.state = BottomSheetBehavior.STATE_HIDDEN
            }

            binding.btnChangeAvtar.setOnClickListener {
                avatarDialog?.behavior?.state = BottomSheetBehavior.STATE_HIDDEN
                changeAvatar()
            }
            val list = getAvtarList()
            profileViewModel.avatars = list

            binding.recyclerAvatars.itemAnimator = null
            if(avatarListAdapter == null){
                avatarListAdapter = ProfileAvatarListAdapter(list,this@ProfileActivity)
            }
            binding.recyclerAvatars.adapter = avatarListAdapter
        }
    }
    private fun changeAvatar() {
        binding.icUser.setImageResource(getAvtar(profileViewModel.selectedAvatar.get()))
        profileViewModel.updateAvtar(profileViewModel.selectedAvatar.get())
    }
}

interface ProfileSelectListener {
    fun setAvatarPosition(pos: Int)
}

