package com.rummytitans.sdk.cardgame.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.databinding.ItemLobbyBonusBinding
import com.rummytitans.sdk.cardgame.models.JoinGameConfirmationModel
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class RummyLobbyBonusAdapter(
    var mList: List<JoinGameConfirmationModel.JoinGameBonus>) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolder(
            ItemLobbyBonusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = mList.size

    fun updateData(list: List<JoinGameConfirmationModel.JoinGameBonus>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(var mBinding: ItemLobbyBonusBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model = mList[position]
            mBinding.executePendingBindings()
        }
    }
}