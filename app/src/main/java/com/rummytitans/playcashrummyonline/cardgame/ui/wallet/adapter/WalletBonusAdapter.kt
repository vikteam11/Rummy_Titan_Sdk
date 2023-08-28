package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.adapter

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemWalletBonusRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.WalletInfoModel
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder

class WalletBonusAdapter(
    var listResponse: MutableList<WalletInfoModel.WalletBonesModel>?,
    val onClickItem: () -> Unit
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    val colorArray = arrayOf(
        R.color.maingreen,
        R.color.faded_orange,
        R.color.lavender_blue, R.color.reddishPink_two,
        R.color.bright_sky_blue
    )
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        val binding =
            ItemWalletBonusRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AvailableCouponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse?.size ?: 0
    }

    fun updateItems(listResponse: MutableList<WalletInfoModel.WalletBonesModel>) {
        this.listResponse?.clear()
        this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class AvailableCouponViewHolder(var mBinding: ItemWalletBonusRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            listResponse?.get(position)?.apply {
                val rawColor= colorArray[if (position<colorArray.size.minus(1))position else colorArray.size/position]
                colorCode=ContextCompat.getColor(context,rawColor)
                mBinding.viewModel =this
            }
            mBinding.root.setOnClickListener {
                if (listResponse?.get(position)?.isbouns == true) onClickItem.invoke()
            }
        }


    }
}