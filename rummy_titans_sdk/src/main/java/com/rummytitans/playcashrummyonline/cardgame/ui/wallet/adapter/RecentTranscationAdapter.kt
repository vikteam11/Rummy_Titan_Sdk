package com.rummytitans.playcashrummyonline.cardgame.ui.wallet.adapter

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemRecentTranscationRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemWithdrawalTransactionRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.TransactionModel
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.TransactionItemNavigator
import com.rummytitans.playcashrummyonline.cardgame.ui.wallet.viewmodel.ItemTransactionViewModel
import com.rummytitans.playcashrummyonline.cardgame.utils.setOnClickListenerDebounce
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder

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
        return if (viewType == 6)//Withdrawal
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
            it.GamePlayUrl = model.GamePlayUrl
            it.Description = model.Description
            it.Unutilized = model.Unutilized
            it.Wiining = model.Wiining
            it.Myteam11Credit = model.Myteam11Credit
            it.Status = model.Status
            it.isDetailAvailable = true
            it.TxnId = model.TxnId
            it.showDetailView = TransactionModel.SHOW_VIEW
            it.statusCode=model.statusCode
            it.BankAccount=model.BankAccount
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
                mBinding.viewReplay.setOnClickListenerDebounce {
                    listner.sendToWebView(model.GamePlayUrl?:"")
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

