package com.rummytitans.sdk.cardgame.ui.payment.adapter

import android.app.Activity
import com.rummytitans.sdk.cardgame.R
import com.rummytitans.sdk.cardgame.databinding.*
import com.rummytitans.sdk.cardgame.models.NewPaymentGateWayModel
import com.rummytitans.sdk.cardgame.ui.payment.PaymentOptionNavigator
import com.rummytitans.sdk.cardgame.ui.payment.viewmodel.*
import com.rummytitans.sdk.cardgame.utils.getCardImage
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder
import com.rummytitans.sdk.cardgame.utils.showToolTip

class AdapterGatewayItem(
    var listResponse: List<NewPaymentGateWayModel.PaymentResponseModel>,
    var listner: PaymentOptionNavigator,
    var themeColor: Int,
    val type: Int = -1,
    val onSelect:(innerPosition:Int)->Unit
) :

    RecyclerView.Adapter<BaseViewHolder>() {

    var selectedPosition = -1
    var mContext: Context? = null

    companion object {
        private const val CREDIT_CARD = 3
        private const val WALLET = 1
        private const val UPI = 4
        private const val NET_BANKING = 2
        private const val OTHER = 5
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        mContext = parent.context
        return when(viewType){
            WALLET ->{
                ViewHolderWallet(
                    ItemNewWalletRummyBinding
                        .inflate(
                            LayoutInflater.from(parent.context), parent,
                            false
                        )
                )
            }

            CREDIT_CARD ->{
                ViewHolderCard(ItemNewCardRummyBinding
                    .inflate(
                        LayoutInflater.from(parent.context), parent,
                        false
                    )
                )
            }

            NET_BANKING ->{
                if(type ==5){
                    ViewHolderNetBank2(ItemAllPaymentRummyBinding
                        .inflate(
                            LayoutInflater.from(parent.context), parent,
                            false
                        )
                    )
                }else{
                    ViewHolderNetBanking(ItemNewNewbankingRummyBinding
                        .inflate(
                            LayoutInflater.from(parent.context), parent,
                            false
                        )
                    )
                }
            }

            UPI ->{
                ViewHolderUpi(ItemNewUpiRummyBinding
                    .inflate(
                        LayoutInflater.from(parent.context), parent,
                        false
                    )
                )
            }
            else ->{
                ViewHolderOther(ItemOtherPaymentRummyBinding
                    .inflate(
                        LayoutInflater.from(parent.context), parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (if (type == 5) listResponse[position].Type else type) {
            1 -> WALLET
            2 -> NET_BANKING
            3 -> CREDIT_CARD
            4 -> UPI
            else -> OTHER
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }


    inner class ViewHolderCard(var mBinding: ItemNewCardRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model = ItemGatewayDataModel(listResponse[position], listner, themeColor)
            mBinding.showDivider = layoutPosition < listResponse.size-1
            mBinding.editText.addTextChangedListener {
                if (it.toString().isNotEmpty())
                    mBinding.editText.error = null
            }
            mBinding.onPayClick = {
                mBinding.model?.let { model ->
                    model.cvv.set(mBinding.editText.text.toString().trim())
                    model.cvv.get()?.let { cvv ->
                        val (_, isMaestroCardType) = getCardImage(model.data.Brand)
                        if (TextUtils.isEmpty(cvv) && !isMaestroCardType) {
                                (mContext as? Activity)?.let {act->
                                    showToolTip(act,mBinding.editText,act.getString(R.string.please_enter_valid_cvv_code))
                                }
                        } else listner.payViaCard(model.data.Token, cvv)
                    }
                }
            }

            if (listResponse[position].isSelected) {
                selectedPosition = position
            }

            mBinding.root.setOnClickListener {
                if (position == selectedPosition)
                    return@setOnClickListener
                if (selectedPosition != -1) {
                    listResponse[selectedPosition].isSelected = false
                    notifyItemChanged(selectedPosition)
                }
                listResponse[position].isSelected = true
                notifyItemChanged(position)
                selectedPosition = position
                onSelect(position)
            }
        }
    }

    inner class ViewHolderWallet(var mBinding: ItemNewWalletRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            val model = listResponse[position]
            mBinding.model = ItemGatewayDataModel(model, listner,themeColor)
            mBinding.showDivider = layoutPosition < listResponse.size-1
            mBinding.executePendingBindings()

            if (listResponse[position].isSelected) {
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
                onSelect(position)
            }

        }
    }

    inner class ViewHolderNetBanking(var mBinding: ItemNewNewbankingRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model =  ItemGatewayDataModel(listResponse[position], listner,themeColor)

            mBinding.executePendingBindings()
        }
    }

    inner class ViewHolderUpi(var mBinding: ItemNewUpiRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model =  ItemGatewayDataModel(listResponse[position], listner,themeColor)
            mBinding.showDivider = layoutPosition < listResponse.size-1

            if (listResponse[position].isSelected) {
                selectedPosition = position
            }
            mBinding.root.setOnClickListener {
                if (position == selectedPosition)
                    return@setOnClickListener
                if (selectedPosition != -1) {
                    listResponse[selectedPosition].isSelected = false
                    notifyItemChanged(selectedPosition)
                }
                listResponse[position].isSelected = true
                notifyItemChanged(position)
                selectedPosition = position
                onSelect(position)
            }
            mBinding.executePendingBindings()
        }
    }

    inner class ViewHolderOther(var mBinding: ItemOtherPaymentRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model = ItemGatewayDataModel(listResponse[position], listner,themeColor)
            mBinding.showDivider = layoutPosition < listResponse.size-1

            if (listResponse[position].isSelected) {
                selectedPosition = position
            }
            mBinding.root.setOnClickListener {
                if (position == selectedPosition)
                    return@setOnClickListener
                if (selectedPosition != -1) {
                    listResponse[selectedPosition].isSelected = false
                    notifyItemChanged(selectedPosition)
                }
                listResponse[position].isSelected = true
                notifyItemChanged(position)
                selectedPosition = position
                onSelect(position)
            }
            mBinding.executePendingBindings()
        }
    }

    inner class ViewHolderNetBank2 constructor(private val mBinding: ItemAllPaymentRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            mBinding.model = ItemGatewayDataModel(listResponse[position], listner,themeColor)
            mBinding.showDivider = layoutPosition < listResponse.size-1

            mBinding.executePendingBindings()

            mBinding.root.setOnClickListener {
                listner.payViaNetBanking(listResponse[position])
            }
        }
    }

}

