package com.rummytitans.playcashrummyonline.cardgame.ui.verifications.module

/*
import com.rummytitans.playcashrummyonline.cardgame.di.scope.FragmentScoped
import com.rummytitans.playcashrummyonline.cardgame.di.viewmodels.ViewModelKey
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.viewmodels.AddressVerificationViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.viewmodels.BankVerificationViewModel
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.viewmodels.PanVerificationViewModel
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import com.rummytitans.playcashrummyonline.cardgame.ui.verifications.*

@Module
abstract class VerificationModule {
    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeFragmentAddressVerifyManual(): FragmentManualAddressVerification

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeFragmentVerificationOption(): FragmentVerificationOption

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeFragmentAddressVerifyWithAadhaar(): AutomaticAddressVerificationFragment

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeFragmentPanVerification(): FragmentPanVerification

    @Binds
    @IntoMap
    @ViewModelKey(PanVerificationViewModel::class)
    abstract fun bindPanVerificationViewModel(viewModel: PanVerificationViewModel): ViewModel

    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeFragmentBankVerification(): FragmentBankVerification

    @Binds
    @IntoMap
    @ViewModelKey(BankVerificationViewModel::class)
    abstract fun bindBankVerificationViewModel(viewModel: BankVerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddressVerificationViewModel::class)
    abstract fun bindAddressVerificationViewModel(viewModel: AddressVerificationViewModel): ViewModel

}*/
