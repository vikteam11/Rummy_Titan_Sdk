package com.rummytitans.sdk.cardgame.ui.verifications.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.databinding.ItemVerificationOptionRummyBinding
import com.rummytitans.sdk.cardgame.models.VerificationOptionModel
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce

class VerificationOptionAdapter(
    var options: ArrayList<VerificationOptionModel>,
    val callback:VerificationOptionNavigator
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null
    private var selectedIndex = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemVerificationOptionRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return options.size
    }

    fun updateItems(listResponse: MutableList<VerificationOptionModel>?) {
        this.options.clear()
        if (!listResponse.isNullOrEmpty())
            this.options.addAll(listResponse)
        notifyDataSetChanged()
    }

    inner class OptionViewHolder(var mBinding: ItemVerificationOptionRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model = options[position]
            if(options[position].isSelect){
                selectedIndex = position
            }
            mBinding.root.setOnClickListenerDebounce {
                if(selectedIndex >=0 && selectedIndex!= position){
                    options[selectedIndex].isSelect = false
                    notifyItemChanged(selectedIndex)
                }
                options[position].isSelect = true
                notifyItemChanged(position)
                callback.onSelectVerificationOption(options[position])
            }
            mBinding.executePendingBindings()
        }
    }

    fun unselectAllItem(){
        options.indexOfFirst { it.isSelect }.let { index->
            if(index >= 0){
                selectedIndex = -1
                options[index].isSelect = false
                notifyItemChanged(index)
            }
        }
    }
}
interface VerificationOptionNavigator{
    fun onSelectVerificationOption(model: VerificationOptionModel)
}