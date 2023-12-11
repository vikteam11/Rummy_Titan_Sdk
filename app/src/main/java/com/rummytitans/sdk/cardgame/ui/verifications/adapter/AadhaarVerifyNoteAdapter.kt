package com.rummytitans.sdk.cardgame.ui.verifications.adapter

import com.rummytitans.sdk.cardgame.databinding.ItemAadhaarVerifyNoteRummyBinding
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AadhaarVerifyNoteAdapter(
    var listResponse: MutableList<String>,
   val showCheckBox:Boolean = false) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemAadhaarVerifyNoteRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ReferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }

    fun updateItems(listResponse: MutableList<String>?) {
        this.listResponse?.clear()
        if (!listResponse.isNullOrEmpty())
            this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class ReferViewHolder(var mBinding: ItemAadhaarVerifyNoteRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.note = listResponse[position]
            mBinding.showCheckBox = showCheckBox
            mBinding.executePendingBindings()
        }
    }
}