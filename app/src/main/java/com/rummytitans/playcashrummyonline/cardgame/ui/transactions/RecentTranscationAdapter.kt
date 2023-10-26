package com.rummytitans.playcashrummyonline.cardgame.ui.transactions

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.models.TransactionModel
import com.rummytitans.playcashrummyonline.cardgame.utils.setOnClickListenerDebounce
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemRecentTranscationRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemWithdrawalTransactionRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.TransactionItemNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel.ItemTransactionViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.SpacesItemDecoration

class RecentTranscationAdapter(
    var listResponse: ArrayList<TransactionModel.TransactionListModel>?,
    var listner: TransactionItemNavigator, val colorCode: String
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var tabName: String = ""
    var currentItemPos = -1
    var oldItemPos = -1
    lateinit var mContext: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        mContext = parent.context
        return if (viewType == 2)//Withdrawal
            WithdrawalTransactionViewHolder(
                ItemWithdrawalTransactionRummyBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )
        else
            RecentTransactionViewHolder(
                ItemRecentTranscationRummyBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            )

    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return listResponse?.elementAtOrNull(position)?.TranType ?: 0
    }

    override fun getItemCount(): Int {
        return listResponse?.size ?: 0
    }

    fun updateItems(
        list: ArrayList<TransactionModel.TransactionListModel>,
        tabtext: String
    ) {
        //this.listResponse?.clear()
        this.listResponse = list
        notifyDataSetChanged()
        tabName = tabtext
    }
    fun updateModel(model: TransactionModel.TransactionListModel) {
        listResponse?.elementAtOrNull(currentItemPos)?.let {
            it.Description = model.Description

            it.Status = model.Status
            it.isDetailAvailable = true
            it.showDetailView = TransactionModel.SHOW_VIEW
            it.statusCode=model.statusCode
            it.BankAccount=model.BankAccount

            it.taxInvoice = model.taxInvoice
            it.GamePlayUrl = model.GamePlayUrl
            it.transactionBreakup = model.transactionBreakup
            it.transactionDetails = model.transactionDetails
            it.closingBalance = model.closingBalance

            Log.d("ClosingBa","Closing Balance ${model.closingBalance.size} ${model.closingBalanceAvailable()}")

        }
        notifyItemChanged(currentItemPos)
        oldItemPos = currentItemPos
    }
    inner class RecentTransactionViewHolder(var mBinding: ItemRecentTranscationRummyBinding) :
        BaseViewHolder(mBinding.root) {

        lateinit var itemViewModel: ItemTransactionViewModel

        override fun onBind(position: Int) {
            listResponse?.elementAtOrNull(position)?.let { model ->
                itemViewModel =
                    ItemTransactionViewModel(
                        model, tabName, {
                            oldItemPos = currentItemPos
                            currentItemPos = position
                            listner.asyncRequestDetails(model)
                            hideOtherView()
                        }, {
                            performToggle(model, position)
                        }, colorCode
                    )

                mBinding.viewModel = itemViewModel

                mBinding.rvBreakup.adapter = TransactionDetailItemAdapter(
                    model.transactionBreakup?: arrayListOf(),
                    TransactionDetailItemAdapter.VIEW_TRAN_BREAKUP,
                    colorCode
                )
                mBinding.rvDetails.adapter = TransactionDetailItemAdapter(
                    model.transactionDetails?: arrayListOf(),
                    TransactionDetailItemAdapter.VIEW_DETAIL,
                    colorCode
                )

                if(mBinding.rvClosingBalance.itemDecorationCount == 0){
                    mBinding.rvClosingBalance.addItemDecoration(SpacesItemDecoration(8))
                }
                mBinding.rvClosingBalance.adapter = TransactionDetailItemAdapter(
                    model.closingBalance?: arrayListOf(),
                    TransactionDetailItemAdapter.VIEW_CLOSING_BALANCE,
                    colorCode
                )

                mBinding.viewReplay.setOnClickListenerDebounce {
                    listner.sendToWebView(model.GamePlayUrl?:"")
                }
                mBinding.btnDownload.setOnClickListenerDebounce {
                    listner.downloadInvoiceUrl(model)
                }

            }

            mBinding.executePendingBindings()
        }

        fun performToggle(model: TransactionModel.TransactionListModel, position: Int) {
            model.showDetailView = if (model.showDetailView == TransactionModel.SHOW_VIEW)
                TransactionModel.HIDE_VIEW
            else
                TransactionModel.SHOW_VIEW
            notifyItemChanged(position)

            if (position != oldItemPos) {
                hideOtherView()
            }
            currentItemPos = position
            oldItemPos = currentItemPos
        }

        fun hideOtherView() {
            listResponse?.elementAtOrNull(oldItemPos)?.showDetailView = TransactionModel.HIDE_VIEW
            notifyItemChanged(oldItemPos)
        }
    }
    inner class WithdrawalTransactionViewHolder(var mBinding: ItemWithdrawalTransactionRummyBinding) :
        BaseViewHolder(mBinding.root) {

        lateinit var itemViewModel: ItemTransactionViewModel

        override fun onBind(position: Int) {
            listResponse?.elementAtOrNull(position)?.let { model ->
                model.withdrawalStatusColor = when (model.statusCode) {
                    2 ->  ContextCompat.getColor(mContext, R.color.fresh_green)
                    3 ->   ContextCompat.getColor(mContext, R.color.tomato_red)
                    else -> ContextCompat.getColor(mContext, R.color.yellow)
                }

                mBinding.tvStatus.post {
                    mBinding.tvStatus.text= model.Status
                }
                itemViewModel =
                    ItemTransactionViewModel(
                        model, tabName,
                        {
                            oldItemPos = currentItemPos
                            currentItemPos = position
                            listner.asyncRequestDetails(model)
                            hideOtherView()
                        },
                        onToggleView={
                            performToggle(model, position)
                        },
                        colorCode= colorCode,
                        sendToWithdrawalDetail={
                            listner.onTrackDetailRequest(model)
                        }
                    )
                mBinding.viewModel = itemViewModel

                mBinding.rvDetails.adapter = TransactionDetailItemAdapter(
                    model.transactionDetails?: arrayListOf(),
                    TransactionDetailItemAdapter.VIEW_DETAIL,
                    colorCode
                )

                if(mBinding.rvClosingBalance.itemDecorationCount == 0){
                    mBinding.rvClosingBalance.addItemDecoration(SpacesItemDecoration(8))
                }
                mBinding.rvClosingBalance.adapter = TransactionDetailItemAdapter(
                    model.closingBalance?: arrayListOf(),
                    TransactionDetailItemAdapter.VIEW_CLOSING_BALANCE,
                    colorCode
                )

            }
            mBinding.executePendingBindings()
        }

        fun performToggle(model: TransactionModel.TransactionListModel, position: Int) {
            model.showDetailView = if (model.showDetailView == TransactionModel.SHOW_VIEW)
                TransactionModel.HIDE_VIEW
            else
                TransactionModel.SHOW_VIEW
            notifyItemChanged(position)

            if (position != oldItemPos) {
                hideOtherView()
            }
            currentItemPos = position
            oldItemPos = currentItemPos
        }

        fun hideOtherView() {
            listResponse?.elementAtOrNull(oldItemPos)?.showDetailView = TransactionModel.HIDE_VIEW
            notifyItemChanged(oldItemPos)
        }
    }
}

