package com.rummytitans.playcashrummyonline.cardgame.ui.deeplink

import com.rummytitans.playcashrummyonline.cardgame.di.viewmodels.ViewModelKey
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class DeepLinkModule {
    @Binds
    @IntoMap
    @ViewModelKey(DeepLinkRummyViewModel::class)
    abstract fun bindCreateContestViewModel(viewModel: DeepLinkRummyViewModel): ViewModel
}