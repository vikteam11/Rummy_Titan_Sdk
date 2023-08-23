package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.adapter

import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemAvailableCouponsRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.AvailableCouponModel
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder

class AvailableCouponsAdapter(var listResponse: MutableList<AvailableCouponModel>?,
                              var selectedColor: String?,
                              private val applyCouponCode: (Int) -> Unit,
                              private val clearCouponCode: () -> Unit,
                              private  val couponApplied: LiveData<Boolean>) :

    RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null
    var prePos: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemAvailableCouponsRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return AvailableCouponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse?.size ?: 0
    }

    fun updateItems(listResponse: MutableList<AvailableCouponModel>) {
        this.listResponse?.clear()
        this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class AvailableCouponViewHolder(var mBinding: ItemAvailableCouponsRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            listResponse?.elementAtOrNull(position)?.let {
                it.selectedColor = selectedColor
                mBinding.model = it
            }
            mBinding.executePendingBindings()

            couponApplied.observe(context as LifecycleOwner, Observer {
                if (it){
                    mBinding.selected = prePos == position
                }else{
                    mBinding.selected = false
                }
            })


            mBinding.tvApply.setOnClickListener {
                if (prePos == -1) {
                    prePos = adapterPosition
                    notifyItemChanged(prePos)
                } else if (prePos != adapterPosition) {
                    notifyItemChanged(prePos)
                    prePos = adapterPosition
                    notifyItemChanged(adapterPosition)
                }

                listResponse?.get(prePos)?.id?.let { it1 -> applyCouponCode(it1) }
            }

            mBinding.ivRemove.setOnClickListener {
                clearCouponCode()
                mBinding.selected = false
                prePos = -1
                notifyItemChanged(prePos)
            }
        }


    }
}

