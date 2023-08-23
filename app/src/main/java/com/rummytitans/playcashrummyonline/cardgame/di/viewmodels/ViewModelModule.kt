package com.rummytitans.playcashrummyonline.cardgame.di.viewmodels

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Module used to define the connection between the framework's [ViewModelProvider.Factory] and
 * our own implementation: [MyTeam11ViewModelFactory].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: MyTeam11ViewModelFactory): ViewModelProvider.Factory
}
