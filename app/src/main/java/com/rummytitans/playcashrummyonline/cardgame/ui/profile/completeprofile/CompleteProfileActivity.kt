package com.rummytitans.playcashrummyonline.cardgame.ui.completeprofile

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.models.AvatarModel
import com.rummytitans.playcashrummyonline.cardgame.models.LoginResponse
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseActivity
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CompleteProfileActivity : BaseActivity(), CompleteProfileNavigator, OnSuggestionClickListner{

    override fun onSuggestionClick(suggestion: String) {
        binding.editTeamName.setText(suggestion)
        viewModel.selectedTeamNameFromSuggestion = suggestion
        viewModel.isSuggestionsUse
    }

    override fun completeProfileSuccess() {
        if (intent.hasExtra(MyConstants.INTENT_PASS_COMING_FROM)) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            startActivity(Intent(this, RummyMainActivity::class.java))
            finishAffinity()
        }
    }

    override fun saveProfile() {
        viewModel.saveProfile(binding.editTeamName.text.toString(), binding.editFullName.text.toString())
    }


    lateinit var binding: com.rummytitans.playcashrummyonline.cardgame.databinding.ActivityCompleteProfileRummyBinding
    lateinit var viewModel: CompleteProfileViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var avatarAdapter: AvatarAdapter
    var suggestionAdapter: SuggestionsAdapter? = null
    var suggestionsList: List<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        if (TextUtils.isEmpty(com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.getLanguage(this))) {
            com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.setLocale(this)
        } else {
            com.rummytitans.playcashrummyonline.cardgame.utils.LocaleHelper.onAttach(this)
        }
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this.viewModelStore,
            viewModelFactory
        ).get(CompleteProfileViewModel::class.java)
        intent?.getStringExtra(MyConstants.CREATE_TEAM_FROM)?.let {
            viewModel.createTeamFromLogin = it.equals(MyConstants.LOGIN, ignoreCase = true)
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_complete_profile_rummy)
        viewModel.navigator = this
        viewModel.navigatorAct = this
        setSnackbarView(binding.fadingSnackbar)
        binding.viewModel = viewModel

        viewModel.myDialog = MyDialog(this)

        setAvatarAdapter()
        setBindingModule()

    }

    fun setAvatarPosition(pos: Int) {
        if (pos == -1) return
        viewModel.selectedAvatar.set(pos + 1)
        viewModel.flagAvatar.set(true)
    }

    override fun fireBranchEvent(eventName: String?, userId: Int) {

    }

    private fun setAvatarAdapter() {
        binding.recyclerAvatars.layoutManager = GridLayoutManager(this, 5)
        binding.recyclerAvatars.adapter = avatarAdapter
        binding.recyclerAvatars.itemAnimator = null
    }

    @SuppressLint("CheckResult", "Recycle")
    private fun setBindingModule() {
        viewModel.getState()
        val loginResponse =
            LoginResponse()
        viewModel.loginResponse = loginResponse
        if (null != intent.getSerializableExtra(MyConstants.INTENT_LOGIN_RESPONSE))
            viewModel.loginResponse = intent.getSerializableExtra(MyConstants.INTENT_LOGIN_RESPONSE) as LoginResponse

        RxTextView.afterTextChangeEvents(binding.editTeamName).skipInitialValue()
            .debounce(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { viewModel.checkTeamName(it.editable().toString()) }

        binding.spnState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.flagState.set(false)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectedState = (view as TextView?)?.text as String? ?: ""
                viewModel.flagState.set(position != 0)
            }
        }
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
        binding.teamNameSuggestion.visibility = View.GONE
        viewModel.teamNameSuggestions.observe(this, Observer {
            suggestionsList = it.split(",")
            if (suggestionsList.isNullOrEmpty()) {
                return@Observer
            }
            binding.teamNameSuggestion.visibility = View.VISIBLE
            binding.teamNameSuggestion.layoutManager = LinearLayoutManager(this@CompleteProfileActivity)
            binding.teamNameSuggestion.adapter =
                SuggestionsAdapter(suggestionsList as MutableList<String>, this@CompleteProfileActivity)

        })

        viewModel.avatarList.set(avatars)
        viewModel.teamNameSuggestions()
        binding.executePendingBindings()
    }
}