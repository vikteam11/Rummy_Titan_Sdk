package com.rummytitans.playcashrummyonline.cardgame.ui.home.adapter

import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemRummyCategoryBinding
import com.rummytitans.playcashrummyonline.cardgame.models.RummyCategoryModel
import com.rummytitans.playcashrummyonline.cardgame.utils.setOnClickListenerDebounce
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder

class RummyCategoryAdapter(
    var mList: List<RummyCategoryModel>,
    val listener:RummyCategoryNavigator,
    ) : RecyclerView.Adapter<BaseViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ViewHolder(
            ItemRummyCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = mList.size

    fun updateData(list: List<RummyCategoryModel>) {
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(var mBinding: ItemRummyCategoryBinding):
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.category = mList[position]
            if(mList[position].Selected){
                selectedPosition = position
            }
            mBinding.root.setOnClickListenerDebounce {
                if(selectedPosition >= 0 && selectedPosition != position){
                    mList[selectedPosition].Selected = false
                    notifyItemChanged(selectedPosition)
                }
                mList[position].Selected = true
                notifyItemChanged(position)
                listener.onCategoryClick(mList[position])
            }
            mBinding.executePendingBindings()
        }
    }
}
interface RummyCategoryNavigator{
    fun onCategoryClick(category: RummyCategoryModel)
    fun onValidLocationFound()
    fun showDialog(message:String)
    fun onSwipeWallet()
}