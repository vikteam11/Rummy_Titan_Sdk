package com.rummytitans.sdk.cardgame.ui.wallet.withdrawal.adapter

import com.rummytitans.sdk.cardgame.databinding.ItemWithdrawProgressRummyBinding
import com.rummytitans.sdk.cardgame.models.WithdrawalDetailModel
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class WithdrawProgressAdapter(var listResponse: List<WithdrawalDetailModel.WithdrawalStatusModel>) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemWithdrawProgressRummyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun getItemCount() = listResponse.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class MyViewHolder constructor(var mBinding: ItemWithdrawProgressRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model= listResponse[position]
            mBinding.isLastItem = position == listResponse.size - 1
            mBinding.executePendingBindings()
        }
    }
}