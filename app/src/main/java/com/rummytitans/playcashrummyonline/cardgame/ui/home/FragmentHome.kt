package com.rummytitans.playcashrummyonline.cardgame.ui.home

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.RummyTitanSDK
import com.rummytitans.playcashrummyonline.cardgame.analytics.AnalyticsKey
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorage
import com.rummytitans.playcashrummyonline.cardgame.databinding.FragmentHomeRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.HeaderItemModel
import com.rummytitans.playcashrummyonline.cardgame.models.RummyCategoryModel
import com.rummytitans.playcashrummyonline.cardgame.models.RummyLobbyModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.RummyMainActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.MainViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseFragmentLocation
import com.rummytitans.playcashrummyonline.cardgame.ui.deeplink.DeepLinkActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.home.adapter.*
import com.rummytitans.playcashrummyonline.cardgame.ui.joinGame.JoinGameBottomSheet
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.RummyNewLoginActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.OnOfferBannerClick
import com.rummytitans.playcashrummyonline.cardgame.utils.MyConstants
import com.rummytitans.playcashrummyonline.cardgame.utils.isMyTeamDeeplink
import com.rummytitans.playcashrummyonline.cardgame.utils.setOnClickListenerDebounce
import com.rummytitans.playcashrummyonline.cardgame.utils.showBottomSheetWebView
import com.rummytitans.playcashrummyonline.cardgame.widget.MyDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class FragmentHome : BaseFragmentLocation(),
    BaseNavigator,
    OnOfferBannerClick,
    RummyCategoryNavigator,
    LobbyNavigator,
    SubcategoryNavigator{

    lateinit var binding: FragmentHomeRummyBinding
    private var timer: Timer? = null
    private var currentOfferPage = 0
    @Inject
    lateinit var prefs: SharedPreferenceStorage
    lateinit var viewModel: HomeViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val rummyLobbyAdapter:RummyLobbyAdapter by lazy {
        RummyLobbyAdapter(arrayListOf(),this)
    }
    private val rummySubcategoryAdapter:RummySubcategoryAdapter by lazy {
        RummySubcategoryAdapter(arrayListOf(),this)
    }
    private val rummyCategoryAdapter:RummyCategoryAdapter by lazy {
        RummyCategoryAdapter(arrayListOf(),this)
    }
    private var fetchingLobbies = false
    private var headerAdapter:WalletOffersAdapter?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        requireActivity().apply {
            if(resources.configuration.orientation== Configuration.ORIENTATION_LANDSCAPE)
                changeActivityOrientation(Configuration.ORIENTATION_PORTRAIT)
        }

        setTheme(inflater)
        viewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.navigator = this
        viewModel.navigatorAct = this
        viewModel.myDialog = MyDialog(requireActivity())
        binding = FragmentHomeRummyBinding.inflate((localInflater ?: inflater), container, false).apply {
            lifecycleOwner = this@FragmentHome
            viewmodel = this@FragmentHome.viewModel
        }
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeData()
        initClick()
        viewModel.getBanner()
        viewModel.getCategories()
        fetchingLobbies = true

        binding.rvCategory.setHasFixedSize(true)
        binding.rvLobbi.setHasFixedSize(true)
        binding.rvSubCategory.setHasFixedSize(true)

        binding.rvCategory.adapter = rummyCategoryAdapter
        binding.rvSubCategory.adapter = rummySubcategoryAdapter
        binding.rvLobbi.adapter = rummyLobbyAdapter

        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ScreenLoadDone, bundleOf(
                AnalyticsKey.Keys.Screen to AnalyticsKey.Screens.HOME
            )
        )
    }


    override fun onResume() {
        super.onResume()
        if(!isHidden){
            (activity as? RummyMainActivity?)?.setNavigationItem(R.id.navigation_home)
        }
        if(!fetchingLobbies){
            viewModel.getCategoryLobbies()
        }
        fetchingLobbies = false
        if(headerAdapter != null){
            startViewPagerScrolling()
        }
    }

    private fun initBannerListener(){

        val horizontalPadding = if((viewModel.headerList.value?.size ?: 0) > 1)40 else 16
        binding.sliderContainer.clipToPadding = false
        binding.sliderContainer.pageMargin = 20
        binding.sliderContainer.setPadding(horizontalPadding, 10, horizontalPadding, 10)

        binding.sliderContainer.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentOfferPage = position
                startViewPagerScrolling()
            }
        })

    }

    private fun initClick() {
        binding.txtRule.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.GameRules,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Home
                )
            )

            activity?.showBottomSheetWebView(
                url = viewModel.selectedCategory.get()?.RuleUrl?:"",
                color = viewModel.selectedColor.get() ?: "",
                getString(R.string.app_name_rummy)
            )
        }
        binding.viewFilter.setOnClickListenerDebounce {
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to AnalyticsKey.Values.SortByValue,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Home
                )
            )
            viewModel.needToSortLobby = true
            viewModel.sortDescending.set(!viewModel.sortDescending.get())
            viewModel.filterLobby()
        }
    }

    private fun observeData() {
        viewModel.headerList.observe(viewLifecycleOwner){
            initBannerListener()
            headerAdapter = WalletOffersAdapter(it, this)
            binding.sliderContainer.adapter = headerAdapter
            startViewPagerScrolling()
        }

        viewModel.categoryList.observe(viewLifecycleOwner){categories->
           if(categories.isNotEmpty()){
               rummyCategoryAdapter.updateData(categories)
               setCategoriesVariant()
           }
        }

        viewModel.filterLobbies.observe(viewLifecycleOwner){lobbies->
            rummyLobbyAdapter.updateData(lobbies)
        }
    }

    private fun setCategoriesVariant() {
        viewModel.selectedCategory.get()?.let {selectedCat->
            viewModel.analyticsHelper.fireEvent(
                AnalyticsKey.Names.ButtonClick, bundleOf(
                    AnalyticsKey.Keys.ButtonName to selectedCat.Name ,
                    AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Home
                )
            )
            viewModel.selectedVariant.set(selectedCat.CardVariants[selectedCat.SelectedVariant])
            rummySubcategoryAdapter.selectedPosition = selectedCat.SelectedVariant
            rummySubcategoryAdapter.updateData(selectedCat.CardVariants?: arrayListOf())
            binding.rvSubCategory.layoutManager = GridLayoutManager(requireActivity(),selectedCat.CardVariants?.size?:1)
            binding.rvSubCategory.adapter = rummySubcategoryAdapter
        }
    }

    private fun startViewPagerScrolling() {
        val numPages = headerAdapter?.count?:0
        val delay = viewModel.viewScrollingTime
        if (viewModel.allowAutoScrolling && numPages > 1) {
            val handler = Handler(Looper.getMainLooper())
            timer?.cancel()
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        if (currentOfferPage == numPages - 1) currentOfferPage = 0
                        else currentOfferPage++
                        binding.sliderContainer.setCurrentItem(currentOfferPage, currentOfferPage!=0)
                    }
                }
            }, delay * 1000, delay * 1000)
        }
    }

    private fun joinGame(lobby: RummyLobbyModel) {
       if(viewModel.connectionDetector.isConnected){
           viewModel.analyticsHelper.fireEvent(
               AnalyticsKey.Names.PlayNowClicked, bundleOf(
                   AnalyticsKey.Keys.GameId to 9 ,
                   AnalyticsKey.Keys.GameName to "Rummy",
                   AnalyticsKey.Keys.PlayerMode to "${viewModel.playerType.get()} Player",
                   AnalyticsKey.Keys.GameType to viewModel.selectedCategory.get()?.Name,
                   AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Home,
               )
           )
           val bottomSheet = JoinGameBottomSheet.newInstance(
               lobby
           )
           bottomSheet.show(childFragmentManager,"")
       }else{
           viewModel.myDialog?.noInternetDialog {
               joinGame(lobby)
           }
       }
    }

    override fun onCategoryClick(category: RummyCategoryModel) {
        if(viewModel.selectedCategory.get()?.CategoryId != category.CategoryId){
            viewModel.cleraLobby()
            viewModel.selectedCategory.set(category)
            viewModel.getCategoryLobbies()
            setCategoriesVariant()
        }
    }

    override fun onClickSubCategory(variant: Int) {
        val btnName = "$variant ${if(variant == 2)" Joker" else " Cards"}"
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.ButtonClick, bundleOf(
                AnalyticsKey.Keys.ButtonName to btnName ,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Home
            )
        )
        viewModel.selectedVariant.set(variant)
        viewModel.filterLobby()
    }

    override fun onOfferClick(headerModel: HeaderItemModel) {
        viewModel.analyticsHelper.fireEvent(
            AnalyticsKey.Names.BannerClicked, bundleOf(
                AnalyticsKey.Keys.BannerId to "",
                AnalyticsKey.Keys.BannerType to headerModel.type,
                AnalyticsKey.Keys.BannerLink to headerModel.deeplink,
                AnalyticsKey.Keys.Screen to AnalyticsKey.Values.Home
            )
        )
        if(headerModel.deeplink.isNotEmpty() && headerModel.deeplink != null) {
            if(headerModel.deeplink.isMyTeamDeeplink()){
                RummyTitanSDK.rummyCallback?.openDeeplink(headerModel.deeplink)
            }else if (headerModel.deeplink.contains("screen=refer", true)) {
                (activity as? RummyMainActivity)?.redirectToTab(R.id.navigation_refer)
            } else  if (headerModel.deeplink.contains("screen=wallet", true)) {
                (activity as? RummyMainActivity)?.redirectToTab(R.id.navigation_wallet)
            } else  if (headerModel.deeplink.contains("screen=rakeback", true)) {
                (activity as? RummyMainActivity)?.redirectToTab(R.id.navigation_rakeback)
            } else {
                startActivity(
                    Intent(
                        requireActivity(),
                        DeepLinkActivity::class.java
                    ).putExtra("deepLink", headerModel.deeplink)
                )
            }
        }
    }

    override fun onSelectLobby(lobby: RummyLobbyModel) {
        if(!lobby.tagShow()){
            if(viewModel.isLocationRequired()){
                viewModel.currentLobby = lobby
                showAllowBottomSheet()
                return
            }
        }
        joinGame(lobby)
    }

    override fun onLocationFound(lat: Double, log: Double) {
        viewModel.saveCurrentTime(userLatLog = "$lat,$log")
    }

    override fun onValidLocationFound() {
        viewModel.currentLobby?.let { lobby->
            joinGame(lobby)
        }
    }

    override fun showDialog(message: String) {
        onRestrictLocationFound(message)
    }

    override fun logoutUser() {
        showError(R.string.err_session_expired)
        activity?.finishAffinity()
        startActivity(Intent(activity, RummyNewLoginActivity::class.java))
    }

    override fun goBack() {
    }

    override fun handleError(throwable: Throwable?) {
        throwable?.message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: String?) {
        message?.let { showErrorMessageView(it) }
    }

    override fun showError(message: Int?) {
        message?.let {  showErrorMessageView(it) }

    }

    override fun showMessage(message: String?) {
        message?.let { showMessageView(it) }
    }

    override fun getStringResource(resourseId: Int) = getString(resourseId)

    override fun onSwipeWallet() {
        val mainViewModel: MainViewModel by activityViewModels()
       // mainViewModel.getWalletDetail()

    }



}