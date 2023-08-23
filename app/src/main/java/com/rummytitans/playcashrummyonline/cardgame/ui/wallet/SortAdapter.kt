package com.rummytitans.playcashrummyonline.cardgame.ui.wallet

import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemSortRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableInt
import androidx.recyclerview.widget.RecyclerView


class SortAdapter(
    val listener: SortItemListener?, val mViewType: Byte = -1
) : RecyclerView.Adapter<BaseViewHolder>() {

    companion object {
        const val VIEW_TRAN_SORT: Byte = 1
    }

    val sortItemList = arrayListOf<SortItemModel>()
    var mLastSortType = -1
    var mLastIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
           return SortViewHolder(
               ItemSortRummyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
    }

    override fun getItemCount() = sortItemList.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    private inner class SortViewHolder constructor(val mBinding: ItemSortRummyBinding) :
        BaseViewHolder(mBinding.root) {

        override fun onBind(position: Int) {
            mBinding.viewModel = sortItemList[position]
            mBinding.sortClick = {
                sortItemList.elementAtOrNull(position)?.let { model ->
                    if (mLastIndex != position) {
                        sortItemList.elementAtOrNull(mLastIndex)?.let {
                            it.sortType.set(0)
                            notifyItemChanged(mLastIndex)
                        }
                    }
                      if (model.sortType.get()==1) {
                        /*sortItemList.elementAtOrNull(0)?.let {
                            it.sortType.set(1)
                            notifyItemChanged(0)
                        }
                        model.sortType.set(0)
                        mLastIndex= 0
                        */

                    }else {
                        model.sortType.set(1)
                      // mLastIndex= position
                    }
                    mLastIndex= position
                    notifyItemChanged(position)
                    listener?.onSortClick(position,if (position==0)false else model.sortType.get() == 1,model.title)
                }
            }
        }
    }

}

data class SortItemModel(val title: String, val sortType: ObservableInt = ObservableInt(0))
interface SortItemListener {
    fun onContestSort(type: Int, sortingTypeDesc: Boolean, isSortOff: Boolean) {}
    fun onSortClick(type: Int, isSortOn: Boolean,sortingName:String) {}

}