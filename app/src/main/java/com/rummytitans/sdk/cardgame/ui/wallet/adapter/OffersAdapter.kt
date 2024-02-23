package com.rummytitans.sdk.cardgame.ui.wallet.adapter

import com.rummytitans.sdk.cardgame.databinding.ItemOffersRummyBinding
import com.rummytitans.sdk.cardgame.models.AddCashOfferModel

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder
import com.rummytitans.sdk.cardgame.ui.wallet.OnOfferClick

class OffersAdapter(var listResponse: MutableList<AddCashOfferModel.AddCash>,
                    var listener: OnOfferClick,
                    var selectedColor: String?,
                    val ticketAvailable:Boolean=false) :

    RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemOffersRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse?.size ?: 0
    }

    fun updateItems(listResponse: MutableList<AddCashOfferModel.AddCash>) {
        this.listResponse?.clear()
        this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class ViewHolder(var mBinding: ItemOffersRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            listResponse?.elementAtOrNull(position)?.let {
                mBinding.model = it
                mBinding.ticketAvailable =ticketAvailable
            }
            mBinding.root.setOnClickListener {
                listResponse[layoutPosition].isSelected =  !listResponse[layoutPosition].isSelected
                notifyItemChanged(layoutPosition)
                listener.onSelectOffer(layoutPosition,listResponse[layoutPosition])
            }
            mBinding.executePendingBindings()
        }
    }
}