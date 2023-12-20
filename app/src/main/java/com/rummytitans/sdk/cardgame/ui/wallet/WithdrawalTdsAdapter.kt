package com.rummytitans.sdk.cardgame.ui.wallet

import com.rummytitans.sdk.cardgame.databinding.ItemWithdrawTdsRummyBinding
import com.rummytitans.sdk.cardgame.models.WithdrawalTdsModel
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder
import com.rummytitans.sdk.cardgame.utils.setOnClickListenerDebounce

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

            binding.ivToolTip.setOnClickListenerDebounce(1000) {view->
                when(listResponse[position].type){
                    1-> listener.showToolTipMessage(view,listResponse[position].value)
                    2->  listener.showWebUrl(listResponse[position].title,listResponse[position].value)
                    3-> listener.showPopUp(listResponse[position].title,listResponse[position].value,"","")
                    4-> {
                        val hyperString = listResponse[position].value2.substringBefore(",")
                        val urlToOpen = listResponse[position].value2.substringAfter(",")
                        listener.showPopUp(listResponse[position].title,listResponse[position].value,urlToOpen,hyperString)
                    }
                }

            }
            binding.executePendingBindings()
        }
    }
}