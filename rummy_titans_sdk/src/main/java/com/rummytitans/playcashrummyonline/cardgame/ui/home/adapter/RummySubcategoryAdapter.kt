package com.rummytitans.playcashrummyonline.cardgame.ui.home.adapter

import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemRummySubcategoryBinding
import com.rummytitans.playcashrummyonline.cardgame.utils.setOnClickListenerDebounce
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder

class RummySubcategoryAdapter(
    var mList: List<Int>,
    val callback:SubcategoryNavigator
    ) : RecyclerView.Adapter<BaseViewHolder>() {

    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolder(
            ItemRummySubcategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = mList.size

    fun updateData(list: List<Int>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(var mBinding: ItemRummySubcategoryBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.variant = mList[position]
            mBinding.selected = position==selectedPosition

            mBinding.root.setOnClickListenerDebounce {
                if(selectedPosition >= 0 && selectedPosition != position){
                    val prevPos = selectedPosition
                    selectedPosition = -1
                    notifyItemChanged(prevPos)
                }
                selectedPosition = position
                notifyItemChanged(selectedPosition)
                callback.onClickSubCategory(mList[position])
            }
            mBinding.executePendingBindings()
        }
    }
}

interface SubcategoryNavigator{
    fun onClickSubCategory(variant: Int)
}