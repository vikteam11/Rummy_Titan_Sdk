package com.rummytitans.sdk.cardgame.ui.refer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.databinding.ItemReferContentRummyBinding
import com.rummytitans.sdk.cardgame.models.ReferContentModel
import com.rummytitans.sdk.cardgame.models.VerificationOptionModel
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class ReferContentAdapter(
    var listContent: ArrayList<ReferContentModel.ReferContent>,
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemReferContentRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listContent.size
    }

    fun updateItems(listContent: MutableList<ReferContentModel.ReferContent>?) {
        this.listContent.clear()
        if (!listContent.isNullOrEmpty())
            this.listContent.addAll(listContent)
        notifyDataSetChanged()
    }

    inner class ContentViewHolder(var mBinding: ItemReferContentRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.content = listContent[position]
            mBinding.executePendingBindings()
        }
    }
}
interface VerificationOptionNavigator{
    fun onSelectVerificationOption(model: VerificationOptionModel)
}