package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemGstCalculationBinding
import com.rummytitans.playcashrummyonline.cardgame.models.GstCalculationModel
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder


class GstCalculationAdapter(
    var listResponse: List<GstCalculationModel.GSTBifurcationItem>,
    ) : RecyclerView.Adapter<BaseViewHolder>() {
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        val binding = ItemGstCalculationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WithdrawalTdsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class WithdrawalTdsViewHolder constructor(private val binding: ItemGstCalculationBinding) :
        BaseViewHolder(binding.root) {

        override fun onBind(position: Int) {
            binding.gstModel = listResponse[position]
            binding.isNote = (position == listResponse.size-1)
            binding.executePendingBindings()
        }
    }
}