package com.rummytitans.playcashrummyonline.cardgame.ui.joinGame

/*
import com.rummytitans.playcashrummyonline.cardgame.di.scope.FragmentScoped
import com.rummytitans.playcashrummyonline.cardgame.di.viewmodels.ViewModelKey
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class JoinGameModule {

  */
/*  @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeFragmentJoin(): JoinGameBottomSheet*//*


    @Binds
    @IntoMap
    @ViewModelKey(JoinContestViewModel::class)
    abstract fun bindJoinContestViewModel(viewModel: JoinContestViewModel): ViewModel
}*/
