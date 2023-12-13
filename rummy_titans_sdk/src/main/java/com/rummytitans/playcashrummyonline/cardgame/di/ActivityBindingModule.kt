package com.rummytitans.playcashrummyonline.cardgame.di
/*
import com.rummytitans.playcashrummyonline.cardgame.di.scope.ActivityScoped
import com.rummytitans.playcashrummyonline.cardgame.ui.MainActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.MainModule
import com.rummytitans.playcashrummyonline.cardgame.ui.deeplink.DeepLinkActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.deeplink.DeepLinkModule
import com.rummytitans.playcashrummyonline.cardgame.games.locationBottomSheet.LocationBottomSheetActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.games.locationBottomSheet.LocationBottomSheetModule
import com.rummytitans.playcashrummyonline.cardgame.ui.games.modules.RummyModule
import com.rummytitans.playcashrummyonline.cardgame.games.rummy.RummyWebViewActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.home.HomeModule
import com.rummytitans.playcashrummyonline.cardgame.ui.joinGame.JoinGameModule
import com.rummytitans.playcashrummyonline.cardgame.ui.launcher.LaunchModule
import com.rummytitans.playcashrummyonline.cardgame.ui.launcher.SplashActivity
import com.rummytitans.playcashrummyonline.cardgame.ui.newlogin.NewLoginModule
import com.rummytitans.playcashrummyonline.cardgame.ui.rakeback.RakeBackModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LocationBottomSheetModule::class])
    internal abstract fun launcherLocationBottomSheetActivity(): LocationBottomSheetActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LaunchModule::class])
    internal abstract fun launcherActivity(): SplashActivity


    @ContributesAndroidInjector(
        modules = [
            MainModule::class,
            HomeModule::class,
            JoinGameModule::class,
            RakeBackModule::class
           ]
    )
    internal abstract fun mainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [DeepLinkModule::class])
    internal abstract fun contributeDeepLinkActivity(): DeepLinkActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [NewLoginModule::class])
    internal abstract fun contributeNewLoginActivity(): NewLoginActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [RummyModule::class])
    internal abstract fun contributeRummyWebViewActivity(): RummyWebViewActivity

}
*/
