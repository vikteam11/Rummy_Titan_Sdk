package com.rummytitans.sdk.cardgame.ui.games.tickets

import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.ActivityRummyGamesTicketBinding
import com.rummytitans.sdk.cardgame.models.GameTicketModel
import com.rummytitans.sdk.cardgame.models.GamesResponseModel
import com.rummytitans.sdk.cardgame.utils.*
import com.rummytitans.sdk.cardgame.widget.MyDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.rummytitans.sdk.cardgame.ui.RummyMainActivity
import com.rummytitans.sdk.cardgame.utils.locationservices.uiModules.CurrentLocationBaseActivity
import javax.inject.Inject

class GamesTicketActivity : CurrentLocationBaseActivity(), GameTicketNavigator {


    lateinit var mBinding: ActivityRummyGamesTicketBinding
    private lateinit var mViewModel: GamesTicketViewModel
    var callingRequired: Byte = STATE_YES

    private companion object {
        const val STATE_YES: Byte = 1
        const val STATE_NO: Byte = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(GamesTicketViewModel::class.java)
        mViewModel.apply {
            navigator = this@GamesTicketActivity
            navigatorAct = this@GamesTicketActivity
            myDialog = MyDialog(this@GamesTicketActivity)
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_rummy_games_ticket)
        mBinding.lifecycleOwner = this
        mBinding.viewModel = mViewModel
        mBinding.implView()
        mViewModel.implApis()

        mBinding.executePendingBindings()
    }

    override fun onResume() {
        super.onResume()
        if (callingRequired == STATE_YES) {
            mViewModel.requestGamesTickets()
        }
    }

    fun ActivityRummyGamesTicketBinding.implView() {
        rvAvaialbleTickets.apply {
            adapter = GamesTicketAdapter(arrayListOf(), this@GamesTicketActivity)
        }
        rvRedeemedTickets.apply {
            adapter = GamesTicketAdapter(arrayListOf(), this@GamesTicketActivity, true)
        }
        mBinding.lifecycleOwner = this@GamesTicketActivity
        mBinding.executePendingBindings()
    }

    fun GamesTicketViewModel.implApis() {
        gamesTicketList.observe(this@GamesTicketActivity, {
            if (it.availableTickets.isEmpty())
                mBinding.availableHeader.visibility = View.GONE
            (mBinding.rvAvaialbleTickets.adapter as? GamesTicketAdapter)?.updateData(it.availableTickets)
            if (it.usersTickets.isEmpty())
                mBinding.userHeadar.visibility = View.GONE
            (mBinding.rvRedeemedTickets.adapter as? GamesTicketAdapter)?.updateData(it.usersTickets)
        })
    }

    override fun onInfoClick(url: String) {
        sendToCloseAbleInternalBrowser(this, url,getString(R.string.rummy_ticket))
    }

    override fun onTicketRedeem(model: GameTicketModel.TicketsItemModel) {
        setResult(MyConstants.GAME_RELOAD)
        finish()
       /* if (model.gameId == 0) {
            mViewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.RedeemGameTicketClicked, bundleOf(
                    AnalyticsKey.Keys.GameName to model.name
                )
            )
            finishAffinity()
            openDeeplink("myteam11deeplink://?screen=games")
            return
        }*/

        //mViewModel.savedTicketModel = model
       /**
        * no need to take location as it is redirect to home
        * and at home without location there is no game play*/
       /* if (mViewModel.isLocationRequired())
       showAllowBottomSheet()
        else*/
       // mViewModel.requestRedeem(model.gameId)
    }

    override fun onGameDetailsRecieve(model: GamesResponseModel.GamesModel) {
        if (TextUtils.isEmpty(model.gameUrl)) return
        setResult(MyConstants.GAME_RELOAD)
        finish()
    }

    override fun onLocationFound(lat: Double, log: Double) {
        mViewModel.saveUserLocation(userLatLog = "$lat,$log")
    }

    override fun onBackPressed() {
        when {
            isTaskRoot -> {
                startActivity(Intent(this, RummyMainActivity::class.java))
                finish()
            }
            else -> super.onBackPressed()
        }
    }

    override  fun onLocationRestricted(descriptionMsg: String){
        onRestrictLocationFound(descriptionMsg)
    }

}

interface GameTicketNavigator {
    fun onTicketRedeem(model: GameTicketModel.TicketsItemModel)
    fun onInfoClick(url: String) {}
    fun onLocationRestricted(descriptionMsg: String)

    fun onGameDetailsRecieve(model: GamesResponseModel.GamesModel)

}