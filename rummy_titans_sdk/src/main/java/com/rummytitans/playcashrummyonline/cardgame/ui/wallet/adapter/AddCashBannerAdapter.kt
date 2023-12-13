package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemAddCashBannerRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.WalletInfoModel
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.OnAddCashBannerClick
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.OnOfferBannerClick
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel.AddCashBannerModel


class AddCashBannerAdapter(var listResponse: MutableList<WalletInfoModel.Offer>?, var click: OnAddCashBannerClick) :
    PagerAdapter() {

    override fun isViewFromObject(view: View, arg1: Any): Boolean {
        return view == arg1
    }

    override fun getCount(): Int {
        return listResponse?.size?:0
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ItemAddCashBannerRummyBinding.inflate(LayoutInflater.from(container.context), container, false).apply {
            viewmodel = AddCashBannerModel(listResponse?.get(position)!!, click)
        }
        container.addView(binding.root)
        binding.executePendingBindings()
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as CardView)
    }

}

