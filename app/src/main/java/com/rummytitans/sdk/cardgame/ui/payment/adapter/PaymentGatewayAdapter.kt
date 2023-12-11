package com.rummytitans.sdk.cardgame.ui.payment.adapter

import com.rummytitans.sdk.cardgame.databinding.ItemGatewayListRummyBinding
import com.rummytitans.sdk.cardgame.models.NewPaymentGateWayModel
import com.rummytitans.sdk.cardgame.ui.payment.PaymentOptionNavigator
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.ui.base.BaseViewHolder
import com.rummytitans.sdk.cardgame.ui.payment.viewmodel.WalletInitializeModel


class PaymentGatewayAdapter(
    var listResponse: ArrayList<NewPaymentGateWayModel.GatewayList>,
    var listner: PaymentOptionNavigator,
    var themeColor: Int
) :
    RecyclerView.Adapter<BaseViewHolder>() {

    var selectedPosition = -1
    var selectedInnerPosition = -1
    var mContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        mContext = parent.context
        val binding = ItemGatewayListRummyBinding
            .inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse.size
    }


    fun updateItems(listResponse: ArrayList<NewPaymentGateWayModel.GatewayList>) {
        if (!listResponse.isNullOrEmpty()) {
            this.listResponse.clear()
            this.listResponse = listResponse
            notifyDataSetChanged()
        }
    }

    fun updateInnerItems(outerPosition :Int, model: WalletInitializeModel?, id:String="", code:String="") {
        listResponse[outerPosition].List.singleOrNull{
            if(model == null) it.Id == id else it.Code == code
        }?.let {
            it.Token = model?.Token?:""
            it.DiffAmount = model?.DiffAmount?:0.0
            it.Balance = (model?.Balance?:"0.0").toDouble()
            it.Linked = model != null
            if(model == null){
                it.isSelected = false
            }
            notifyItemChanged(outerPosition)
        }
    }

    inner class ViewHolder(var mBinding: ItemGatewayListRummyBinding) :
        BaseViewHolder(mBinding.root) {
        override fun onBind(position: Int) {
            val model = listResponse[layoutPosition]

            val list =  if((model.Type ==1 || model.Type == 2))
                model.List.filter { it.IsShort }.toList()
            else model.List

            mBinding.model = model
            mBinding.showDivider = list.isNotEmpty()
            mBinding.itemList.itemAnimator = null


            mBinding.itemList.layoutManager = if(model.Type ==2){
                val span = if(list.isNotEmpty())list.size else 1
                GridLayoutManager(mBinding.root.context,span)
            }else
                LinearLayoutManager(mBinding.root.context)

            mBinding.itemList.adapter = AdapterGatewayItem(
                list,
                listner,
                themeColor,
                model.Type
            ){pos->
                if(selectedPosition != -1 && selectedPosition != layoutPosition && selectedInnerPosition != -1){
                    listResponse[selectedPosition].List[selectedInnerPosition].isSelected = false
                    notifyItemChanged(selectedPosition)
                }

                selectedInnerPosition = pos
                selectedPosition = layoutPosition
            }

            mBinding.inAddMore.layoutAddMore.setOnClickListener {
                when(model.Type){
                    1 ->{
                        listner?.showMore(model.Type,model.List)
                    }

                    2 ->{
                        listner?.showMore(model.Type,model.List)
                    }

                    3 ->{
                        listner?.addNewCard()
                    }

                    4 ->{
                        listner?.openAddUpiDialog()
                    }
                }
            }
            mBinding.executePendingBindings()
        }
    }
}

