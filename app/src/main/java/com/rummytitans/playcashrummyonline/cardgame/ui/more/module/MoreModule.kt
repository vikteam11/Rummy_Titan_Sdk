package com.rummytitans.playcashrummyonline.cardgame.ui.more.module

/*
import com.rummytitans.playcashrummyonline.cardgame.di.scope.FragmentScoped
import com.rummytitans.playcashrummyonline.cardgame.di.viewmodels.ViewModelKey
import com.rummytitans.playcashrummyonline.cardgame.ui.more.FragmentMore
import com.rummytitans.playcashrummyonline.cardgame.ui.more.FragmentWebViews
import com.rummytitans.playcashrummyonline.cardgame.ui.more.MoreViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.profile.modules.ProfileFragmentsModule
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.FragmentFeedback
import com.rummytitans.playcashrummyonline.cardgame.ui.more.FragmentSupport
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class MoreModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [ProfileFragmentsModule::class])
    abstract fun contributeFragmentMore(): FragmentMore

    @Binds
    @IntoMap
    @ViewModelKey(MoreViewModel::class)
    abstract fun bindMoreViewModel(viewModel: MoreViewModel): ViewModel

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeFragmentSupport(): FragmentSupport

    @Binds
    @IntoMap
    @ViewModelKey(SupportViewModel::class)
    abstract fun bindSupportViewModel(viewModel: SupportViewModel): ViewModel

    @FragmentScoped
    @ContributesAndroidInjector
    internal abstract fun contributeFragmentWebViews(): FragmentWebViews

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeFragmentFeedback(): FragmentFeedback

    @Binds
    @IntoMap
    @ViewModelKey(FeedbackViewModel::class)
    abstract fun bindFeedbackViewModel(viewModel: FeedbackViewModel): ViewModel

}*/
