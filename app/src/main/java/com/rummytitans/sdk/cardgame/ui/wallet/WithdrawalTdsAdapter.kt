package com.rummytitans.sdk.cardgame.ui.wallet

import com.rummytitans.sdk.cardgame.databinding.ItemWithdrawTdsRummyBinding
import com.rummytitans.sdk.cardgame.models.WithdrawalTdsModel
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class WithdrawalTdsAdapter(
    var listResponse: List<WithdrawalTdsModel>,
    val listener:WithDrawTdsListener
) : RecyclerView.Adapter<BaseViewHolder>() {
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        val binding = ItemWithdrawTdsRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WithdrawalTdsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class WithdrawalTdsViewHolder constructor(private val binding: ItemWithdrawTdsRummyBinding) :
        BaseViewHolder(binding.root) {

        override fun onBind(position: Int) {
            binding.tdsModel = listResponse[position]
            binding.isFinalAmount = (position == listResponse.size-1)

            binding.toolTip.setOnClickListener {view->
                listener.showToolTipMessage(view,listResponse[position].toolTip)
            }
            binding.executePendingBindings()
        }
    }
}