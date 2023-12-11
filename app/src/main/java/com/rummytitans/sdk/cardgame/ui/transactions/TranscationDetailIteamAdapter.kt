package com.rummytitans.sdk.cardgame.ui.transactions

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.ItemTransactionBonusRummyBinding
import com.rummytitans.sdk.cardgame.databinding.ItemTransactionBreakupRummyBinding
import com.rummytitans.sdk.cardgame.databinding.ItemTransactionBreakupRummyBindingImpl
import com.rummytitans.sdk.cardgame.databinding.ItemTransactionDetailsRummyBinding
import com.rummytitans.sdk.cardgame.models.KeyValuePairModel
import com.rummytitans.sdk.cardgame.models.WalletInfoModel
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class TransactionDetailItemAdapter(
    var listResponse: List<KeyValuePairModel>?,
    val viewType: Int = VIEW_DETAIL,
    val colorCode:String,
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    companion object{
        const val VIEW_DETAIL =1
        const val VIEW_TRAN_BREAKUP =2
        const val VIEW_CLOSING_BALANCE =3
    }

    lateinit var mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        mContext = parent.context
        return when(this.viewType){
            VIEW_TRAN_BREAKUP ->{
                TranBreakUpViewHolder(
                    ItemTransactionBreakupRummyBindingImpl.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            VIEW_CLOSING_BALANCE ->{
                ClosingBalanceViewHolder(
                    ItemTransactionBonusRummyBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
            else -> {
                DetailViewHolder(
                    ItemTransactionDetailsRummyBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }
        }

    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse?.size ?: 0
    }


    inner class TranBreakUpViewHolder(var mBinding: ItemTransactionBreakupRummyBinding) :
        BaseViewHolder(mBinding.root) {

        override fun onBind(position: Int) {
            listResponse?.elementAtOrNull(position)?.let { model ->
                model.colorCode = getAmountColorCode(mBinding.root.context,model.value)
                mBinding.model = model
            }
            mBinding.executePendingBindings()
        }

    }

    inner class ClosingBalanceViewHolder(var mBinding: ItemTransactionBonusRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            listResponse?.elementAtOrNull(position)?.let { model ->
                val walletBonusModel = WalletInfoModel.WalletBonesModel()
                walletBonusModel.name = model.key
                walletBonusModel.value = model.value
                walletBonusModel.colorCode = getColorCode(mBinding.root.context,position)

                mBinding.viewModel = walletBonusModel
            }

            mBinding.executePendingBindings()
        }

    }

    inner class DetailViewHolder(var mBinding: ItemTransactionDetailsRummyBinding) :
        BaseViewHolder(mBinding.root) {

        override fun onBind(position: Int) {
            listResponse?.elementAtOrNull(position)?.let { model ->
                model.colorCode = getAmountColorCode(mBinding.root.context,model.value)
                mBinding.model = model
            }
            mBinding.executePendingBindings()
        }

    }

    fun getColorCode(context: Context,pos:Int):Int{
       return when(pos){
            0->{
                ContextCompat.getColor(context, R.color.rummy_maingreen)
            }
            1->{
                ContextCompat.getColor(context, R.color.faded_orange)
            }
            2->{
                ContextCompat.getColor(context,R.color.lavender_blue)
            }
            3->{
                ContextCompat.getColor(context, R.color.periwinkle)
            }
            else ->{
                ContextCompat.getColor(context, R.color.periwinkle)
            }
        }
    }

    fun getAmountColorCode(context: Context,amountStr:String):Int{
        return when{
            amountStr.startsWith("+")->{
                ContextCompat.getColor(context, R.color.alertGreen)
            }
            amountStr.startsWith("-")->{
                ContextCompat.getColor(context, R.color.alertRed)
            }
            else ->{
               ContextCompat.getColor(context, R.color.text_color2)
            }
        }
    }

}

