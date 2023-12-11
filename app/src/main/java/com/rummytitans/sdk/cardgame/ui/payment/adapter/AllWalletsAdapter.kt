package com.rummytitans.sdk.cardgame.ui.payment.adapter


import com.rummytitans.sdk.cardgame.databinding.ItemAllPaymentRummyBinding
import com.rummytitans.sdk.cardgame.databinding.ItemNewWalletRummyBinding
import com.rummytitans.sdk.cardgame.models.NewPaymentGateWayModel
import com.rummytitans.sdk.cardgame.ui.payment.PaymentOptionNavigator
import com.rummytitans.sdk.cardgame.ui.payment.viewmodel.*
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder

class AllWalletsAdapter(
    var listResponse: MutableList<NewPaymentGateWayModel.PaymentResponseModel>,
    var listener: PaymentOptionNavigator,
    var type: Int, val themeColor:Int
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    lateinit var context: Context
    var selectedPosition = -1

    init {
        selectedPosition = listResponse.indexOfFirst { it.isSelected }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        return if(type==1){
            ViewHolderWallet(
                ItemNewWalletRummyBinding
                    .inflate(
                        LayoutInflater.from(parent.context), parent,
                        false
                    )
            )
        }else{
            val binding = ItemAllPaymentRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CategoriesViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    fun updateItems(model: WalletInitializeModel?, id:String="", code:String) {
        val pos = listResponse.indexOfFirst{
            if(model == null) it.Id == id else it.Code == code
        }
        if(pos >=0){
            listResponse[pos]?.let {
                it.Token = model?.Token?:""
                it.Balance = (model?.Balance?:"0.0").toDouble()
                it.DiffAmount = model?.DiffAmount?:0.0
                it.Linked = model != null
                if(model == null){
                    it.isSelected = false
                }
                notifyItemChanged(pos)
            }
        }
    }

    inner class ViewHolderWallet(var mBinding: ItemNewWalletRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            val model  = listResponse[position]
            mBinding.model = ItemGatewayDataModel(model, listener,themeColor)
            mBinding.showDivider = layoutPosition < listResponse.size-1

            mBinding.executePendingBindings()

            if (model.isSelected) {
                selectedPosition = position
            }
            mBinding.root.setOnClickListener {
                if(model.DirectDebit && !model.Linked){
                    return@setOnClickListener
                }

                if (position == selectedPosition)
                    return@setOnClickListener
                if (selectedPosition != -1) {
                    listResponse[selectedPosition].isSelected = false
                    notifyItemChanged(selectedPosition)
                }
                listResponse[position].isSelected = true
                notifyItemChanged(position)
                selectedPosition = position
            }

        }
    }

    inner class CategoriesViewHolder constructor(private val mBinding: ItemAllPaymentRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model = ItemGatewayDataModel(listResponse[position], listener,themeColor)
            mBinding.showDivider = layoutPosition < listResponse.size-1
            mBinding.executePendingBindings()

            mBinding.root.setOnClickListener {
                listener.payViaNetBanking(listResponse[position])
            }
        }
    }
}