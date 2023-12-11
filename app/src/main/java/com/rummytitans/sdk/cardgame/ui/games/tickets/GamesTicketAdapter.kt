package com.rummytitans.sdk.cardgame.ui.games.tickets

import com.rummytitans.sdk.cardgame.databinding.ItemGamesTicketRummyBinding
import com.rummytitans.sdk.cardgame.models.GameTicketModel
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class GamesTicketAdapter(
    var mList: List<GameTicketModel.TicketsItemModel>
    , val clickListener: GameTicketNavigator
    ,val isUsed:Boolean=false) :
    RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolder(
            ItemGamesTicketRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = mList.size

    fun updateData(list: List<GameTicketModel.TicketsItemModel>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(var mBinding: ItemGamesTicketRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mList.elementAtOrNull(position)?.let {
                mBinding.isUsed=isUsed
                mBinding.model = it
            }
            mBinding.btnRedeem.setOnClickListener {
                mList.elementAtOrNull(adapterPosition)?.let {
                    clickListener.onTicketRedeem(it)
                }
            }
        }
    }
}