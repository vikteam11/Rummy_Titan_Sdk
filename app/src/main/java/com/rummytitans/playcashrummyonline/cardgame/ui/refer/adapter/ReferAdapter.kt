package com.rummytitans.playcashrummyonline.cardgame.ui.refer.adapter

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemReferEarnRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.utils.getAvtar
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder
import com.rummytitans.playcashrummyonline.cardgame.ui.refer.ReferItemClick
import com.rummytitans.playcashrummyonline.cardgame.ui.refer.viewmodel.ItemReferModel

class ReferAdapter(
    var listResponse: MutableList<ItemReferModel>?,
    var click: ReferItemClick,
    val avtarId:Int
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null
    private var currentOpenViewPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == 0) {
            val binding = ItemReferEarnRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            context = parent.context
            ReferViewHolder(binding)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_invite_friends_rummy, parent, false)
            itemView.setOnClickListener {
                click.onItemClick(-1)
            }
            InviteFriendViewHolder(itemView)
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }


    override fun getItemViewType(position: Int): Int {
        return if (position < listResponse?.size?:0) {
            0
        } else 1
    }

    override fun getItemCount(): Int {
        return listResponse?.size?:0 + 1
    }

    fun updateItems(listResponse: MutableList<ItemReferModel>?) {
        this.listResponse?.clear()
        if (!listResponse.isNullOrEmpty())
            this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class ReferViewHolder(var mBinding: ItemReferEarnRummyBinding) :
        BaseViewHolder(mBinding.root),
        ToggleViewListener {
        override fun onBind(position: Int) {
            listResponse?.get(position)?.let {
                mBinding.model = it
                mBinding.listener=this
                mBinding.imgUser.setImageResource(getAvtar(it.data.AvtaarId))
                kotlin.runCatching {
                    val finalAmt=it.data.EarnAdmount.toDouble().plus(it.data.SignUpAmount.toDouble())
                    (context?.getString(R.string.rupees)+finalAmt).also { mBinding.txtEarnAmount.text = it }
                }.onFailure {
                    (context?.getString(R.string.rupees)+"0.0").also { mBinding.txtEarnAmount.text = it }
                }
                mBinding.executePendingBindings()
            }
        }

        override fun toggleView(){
            listResponse?.elementAtOrNull(adapterPosition)?.let{
                it.isViewHide.set(!it.isViewHide.get())
            }
            /*Hide if any other view open */
            if(currentOpenViewPosition >= 0 && currentOpenViewPosition != adapterPosition){
                listResponse?.elementAtOrNull(currentOpenViewPosition)?.let{
                    it.isViewHide.set(true)
                }
            }
            currentOpenViewPosition = adapterPosition
        }
    }

    inner class InviteFriendViewHolder internal constructor(view: View) : BaseViewHolder(view) {
        override fun onBind(position: Int) {
        }
    }

}

interface ToggleViewListener{
    fun toggleView()
}