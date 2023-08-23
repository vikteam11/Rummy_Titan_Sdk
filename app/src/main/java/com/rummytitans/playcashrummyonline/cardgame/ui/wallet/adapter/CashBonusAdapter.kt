package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.adapter

import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemCashBonusRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.CashBonusModel
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder

class CashBonusAdapter(var listResponse: ArrayList<CashBonusModel>?) :
    RecyclerView.Adapter<BaseViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemCashBonusRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CashBonusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse?.size ?: 0
    }

    fun updateItems(listResponse: ArrayList<CashBonusModel>) {
        this.listResponse?.clear()
        this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class CashBonusViewHolder(var mBinding: ItemCashBonusRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model = listResponse?.get(position)!!
            mBinding.executePendingBindings()
        }
    }

}

