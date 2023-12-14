package com.rummytitans.sdk.cardgame.ui.wallet.winning_conversion.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.databinding.ItemWinningConversionRangeBinding
import com.rummytitans.sdk.cardgame.models.WinningConversionContentModel
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce


class WinningConversionRangeAdapter(
    var listResponse:List<WinningConversionContentModel.WinningConversionRangeModel>,
    val color:String,
    val callback: WinningConversionRangeCallback
) :
        RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null
    var selected: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemWinningConversionRangeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return WinningRangeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }

    fun selectRange(position: Int){
        if(position == selected)
            return

        if(selected >= 0){
            listResponse[selected].selected = false
            notifyItemChanged(selected)
        }

        if(position >= 0){
            listResponse[position].selected = true
            notifyItemChanged(position)
        }
        selected = position
    }
    fun getSelectedRange()=if(selected >=0){
        listResponse[selected]
    }else{
        listResponse[0]
    }
    inner class WinningRangeViewHolder(var mBinding: ItemWinningConversionRangeBinding) :
            BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            val range = listResponse[position]
            mBinding.model = range
            mBinding.root.setOnClickListenerDebounce {
                if(range.enable){
                    selectRange(position)
                    callback.onSelectRange(range)
                }
            }
            mBinding.executePendingBindings()
        }
    }
}
interface WinningConversionRangeCallback{
    fun onSelectRange(model: WinningConversionContentModel.WinningConversionRangeModel){}
}
