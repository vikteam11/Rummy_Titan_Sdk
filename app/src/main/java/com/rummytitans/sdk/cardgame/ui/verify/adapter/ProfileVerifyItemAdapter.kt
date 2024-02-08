package com.rummytitans.sdk.cardgame.ui.verify.adapter

import com.rummytitans.sdk.cardgame.databinding.ItemVerfificationRummyBinding
import com.rummytitans.sdk.cardgame.databinding.ItemVerfificationProfileRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder
import com.rummytitans.sdk.cardgame.ui.profile.verify.ProfileVerificationItem
import com.rummytitans.sdk.cardgame.ui.verify.VerificationNavigator
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ProfileVerifyItemAdapter(
    var listResponse: MutableList<ProfileVerificationItem>,
    private val fromProfile:Boolean=false,
    val listener : VerificationNavigator?= null) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
         return if(fromProfile){
            val binding = ItemVerfificationProfileRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
             context = parent.context
             ProfileViewHolder(binding)
        }else{
             val binding =  ItemVerfificationRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
             context = parent.context
             VerifyViewHolder(binding)
        }


    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }

    fun updateItems(listResponse: MutableList<ProfileVerificationItem>?) {
        this.listResponse?.clear()
        if (!listResponse.isNullOrEmpty())
            this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class VerifyViewHolder(var mBinding: ItemVerfificationRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.data = listResponse[position]

            /*
            * set unique viewId at dynamically for automation
            * */
            val view = mBinding.root.findViewWithTag<View>("button")
            view.id = listResponse[position].buttonId
            view?.setOnClickListenerDebounce {
                if(!listResponse[position].isDisabled()) {
                    listener?.onVerificationItemClick(listResponse[position])
                }
            }

            mBinding.btnDelete.setOnClickListenerDebounce{
                listener?.onDeleteVerificationItem(listResponse[position])
            }
            mBinding.imgVerify.setOnClickListenerDebounce{
                if(!listResponse[position].isVerified){
                    listener?.onClickWarning(it,listResponse[position])
                }
            }
            mBinding.root.setOnClickListenerDebounce {
                if(listResponse[position].isDisabled()){
                    listener?.onVerificationItemClick(listResponse[position])
                }
            }
            mBinding.executePendingBindings()
        }
    }
    inner class ProfileViewHolder(var mBinding: ItemVerfificationProfileRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.data = listResponse[position]
            mBinding.hideNextArrow = position == listResponse.size-1
            mBinding.executePendingBindings()
        }
    }
}