package com.rummytitans.sdk.cardgame.ui.wallet.winning_conversion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.databinding.ItemWinningConversionBenifitBinding
import com.rummytitans.sdk.cardgame.models.WinningConversionContentModel
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class WinningConversionBenefitAdapter(var listResponse: List<WinningConversionContentModel.WinningConversionBenefitModel>) :
        RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null
    var prePos: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemWinningConversionBenifitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return BenefitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse.size ?: 0
    }

    inner class BenefitViewHolder(var mBinding: ItemWinningConversionBenifitBinding) :
            BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model = listResponse[position]
            mBinding.executePendingBindings()
        }


    }
}

