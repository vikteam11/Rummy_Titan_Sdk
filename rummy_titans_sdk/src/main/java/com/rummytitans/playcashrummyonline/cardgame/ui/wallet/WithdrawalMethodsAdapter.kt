package com.rummytitans.playcashrummyonline.cardgame.ui.wallet

import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemWithdrawalMethodRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.WithdrawalMethodModel
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder

class WithdrawalMethodsAdapter(
    var listResponse: List<WithdrawalMethodModel>,
    val listener: WithdrawalMethodSelectionListener,
    val colorCode:ObservableField<String?>
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    var selectedButton: CompoundButton? = null
    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        val binding = ItemWithdrawalMethodRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WithdrawalMethodsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class WithdrawalMethodsViewHolder constructor(private val playerBinding: ItemWithdrawalMethodRummyBinding) :
        BaseViewHolder(playerBinding.root) {

        override fun onBind(position: Int) {
            listResponse.elementAtOrNull(position)?.let {
                playerBinding.viewModel = it
            }
        }
    }

    var isDataChanging = false
}