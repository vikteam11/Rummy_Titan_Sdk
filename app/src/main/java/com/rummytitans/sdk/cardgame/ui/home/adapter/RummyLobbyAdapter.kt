package com.rummytitans.sdk.cardgame.ui.home.adapter

import com.rummytitans.sdk.cardgame.databinding.ItemRummyLobbyBinding
import com.rummytitans.sdk.cardgame.models.RummyLobbyModel
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class RummyLobbyAdapter(
    var mList: List<RummyLobbyModel>,
    val callback:LobbyNavigator
    ) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolder(
            ItemRummyLobbyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = mList.size

    fun updateData(list: List<RummyLobbyModel>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(var mBinding: ItemRummyLobbyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.lobby = mList[position]

            mBinding.txtJoin.setOnClickListenerDebounce {
                callback.onSelectLobby(mList[position])
            }
            mBinding.executePendingBindings()
        }
    }
}

interface LobbyNavigator{
    fun onSelectLobby(lobby: RummyLobbyModel)
}