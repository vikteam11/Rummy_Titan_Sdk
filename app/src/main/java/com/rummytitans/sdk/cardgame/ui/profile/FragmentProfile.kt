package com.rummytitans.sdk.cardgame.ui.profile

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.analytics.AnalyticsKey
import com.rummytitans.sdk.cardgame.models.AvatarModel
import com.rummytitans.sdk.cardgame.ui.base.BaseFragment
import com.rummytitans.sdk.cardgame.ui.common.CommonFragmentActivity
import com.rummytitans.sdk.cardgame.ui.profile.completeprofile.AvatarAdapter
import com.rummytitans.sdk.cardgame.ui.profile.info.DBUpdateNavigortor
import com.rummytitans.sdk.cardgame.ui.profile.info.viewmodel.ProfileInfoViewModel
import com.rummytitans.sdk.cardgame.ui.profile.updatePhone.UpdatePhoneActivity
import com.rummytitans.sdk.cardgame.ui.verify.viewmodel.VerifyViewModel
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.rummytitans.sdk.cardgame.RummyTitanSDK
import com.rummytitans.sdk.cardgame.databinding.ActivityProfileRummyBinding
import com.rummytitans.sdk.cardgame.databinding.RummyDialogUpdateTeamNameBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseNavigator
import com.rummytitans.sdk.cardgame.ui.verify.adapter.ProfileVerifyItemAdapter
import kotlinx.android.synthetic.main.activity_profile_rummy.*
import javax.inject.Inject

class FragmentProfile : BaseFragment(),
    BaseNavigator, ProfileSelectListener, DBUpdateNavigortor {

    companion object {
        val UPDATE_PROFILE = 12221
    }


    lateinit var profileViewModel: ProfileViewModel
    lateinit var profileInfoViewModel: ProfileInfoViewModel
    lateinit var verificationViewModel: VerifyViewModel
    lateinit var binding: ActivityProfileRummyBinding

    @Inject
    lateinit var avatarAdapter: AvatarAdapter

    private val REQUSET_CODE_VERIFY_DETAIL = 102
    private val REQUSET_CODE_EDIT_PROFILE = 101
    private var currentChipID = -1
    private var mTeamUpdateBinding: RummyDialogUpdateTeamNameBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().apply {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                changeActivityOrientation(Configuration.ORIENTATION_PORTRAIT)
        }
        profileViewModel =
            ViewModelProvider(
                this
            ).get(ProfileViewModel::class.java)
        verificationViewModel =
            ViewModelProvider(
                this
            ).get(VerifyViewModel::class.java)
        profileInfoViewModel =
            ViewModelProvider(
                this
            ).get(ProfileInfoViewModel::class.java)
        binding = ActivityProfileRummyBinding.inflate((localInflater ?: inflater), container, false)

        binding.lifecycleOwner = this
        binding.profileViewModel = profileViewModel
        binding.profileInfoViewModel = profileInfoViewModel
        binding.verificationViewModel = verificationViewModel

        profileInfoViewModel.navigator = this
        verificationViewModel.navigator = this
        profileViewModel.navigator = this
        setAvatarAdapter()
        binding.executePendingBindings()
        return binding.root
    }

    override fun requestData() {
        verificationViewModel.fetchVerificationData()
        profileInfoViewModel.fetchProfileData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verificationViewModel.fetchVerificationData()
        profileInfoViewModel.fetchProfileData()
        profileInfoViewModel.navigatorAct = this

        profileInfoViewModel.data.observe(viewLifecycleOwner, Observer {
            binding.profileInfoViewModel = profileInfoViewModel
            profileInfoViewModel.prefs.avtarId = it.AvtarId
            profileInfoViewModel.prefs.userName = it.Name
            profileInfoViewModel.prefs.userTeamName = it.TeamNmae
            profileInfoViewModel.prefs.refertext = it.REferAmount
            avatarAdapter.setSelected(it.AvtarId - 1)
            setAvatarPosition(it.AvtarId)
            icUser.setImageResource(getAvtar(it.AvtarId))
        })

        binding.changePhonno.setOnClickListener {
            onEditMobileClick()
        }



        txtVerifyAccount.setOnClickListener {

            verificationViewModel.verificationInfo.value?.let {
                val step = when {
                    !it.EmailItem.Verify -> AnalyticsKey.Values.VerificationStepEmail
                    !it.PancardItem.Verify -> AnalyticsKey.Values.VerificationStepPan
                    !it.BankItem.Verify -> AnalyticsKey.Values.VerificationStepBank
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
                Intent(requireContext(), CommonFragmentActivity::class.java)
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
                Intent(requireContext(), CommonFragmentActivity::class.java)
                    .putExtra(MyConstants.INTENT_PASS_COMMON_TYPE, "editprofile"),
                REQUSET_CODE_EDIT_PROFILE
            )
        }
        observerVerificationItem()
    }

    private fun observerVerificationItem() {
        verificationViewModel.verificationInfo.observe(viewLifecycleOwner){
            val list = verificationViewModel.getVerificationItems(true)
            binding.rvVerification.layoutManager = GridLayoutManager(requireActivity(),list.size)
            binding.rvVerification.adapter = ProfileVerifyItemAdapter(verificationViewModel.getVerificationItems(true),
                true)
        }
    }

    override fun onEditMobileClick() {
        profileViewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.MobileNoUpdated, bundleOf(
                AnalyticsKey.Keys.MobileNo to profileInfoViewModel.profileInfo.value?.Mobile,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.Profile
            )
        )
        startActivityForResult(
            Intent(requireContext(), UpdatePhoneActivity::class.java), UPDATE_PROFILE
        )
    }

    override fun goBack() {

    }

    override fun handleError(throwable: Throwable?) {
    }

    override fun showError(message: String?) {
    }

    override fun showError(message: Int?) {
    }

    override fun showMessage(message: String?) {
    }

    override fun logoutUser() {
        RummyTitanSDK.rummyCallback?.logoutUser()
    }

    override fun getStringResource(resourseId: Int): String {
        return getString(resourseId)
    }


    override fun setAvatarPosition(pos: Int) {
        if (pos == -1) return
        profileViewModel.selectedAvatar.set(pos + 1)
    }

    private fun setAvatarAdapter() {
        val avatars = ArrayList<AvatarModel>()
        val imageArray = resources.obtainTypedArray(R.array.rummy_avatar_image)
        for (i in 0 until imageArray.length()) {
            avatars.add(
                AvatarModel(
                    imageArray.getResourceId(
                        i,
                        0
                    ), false
                )
            )
        }
        profileViewModel.avatarList.set(avatars)
        val spaceDecoration = SpacesItemDecoration(5)
        avatarAdapter.listener = this

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
                profileInfoViewModel.checkTeamName(it.toString())
            }

            MyDialog(requireActivity()).getMyDialog(root).apply {
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
                isTeamAvailable.observe(viewLifecycleOwner, Observer {
                    inputTeamName.apply {
                        if (!TextUtils.isEmpty(teamName.get())) {
                            val (msg, color) = if (it)
                                Pair(R.string.txt_team_name_correct, R.color.light_olive_green)
                            else
                                Pair(R.string.team_name_not_available, R.color.error_code)
                            val trueColor = ContextCompat.getColor(requireContext(), color)
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

}