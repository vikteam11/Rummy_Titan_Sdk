package com.rummytitans.playcashrummyonline.cardgame.ui.more.adapter

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemSupportRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder
import com.rummytitans.playcashrummyonline.cardgame.ui.more.SupportModel
import com.rummytitans.playcashrummyonline.cardgame.ui.more.SupportClick
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SupportAdapter(
    var listResponse: ArrayList<SupportModel>?,
    var listner: SupportClick
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemSupportRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return RecentTransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse?.size ?: 0
    }

    inner class RecentTransactionViewHolder(var mBinding: ItemSupportRummyBinding) :
        BaseViewHolder(mBinding.root) {

        override fun onBind(position: Int) {
            mBinding.model = listResponse!![position]
            mBinding.executePendingBindings()

            mBinding.root.setOnClickListener {
                when (listResponse?.get(position)?.title) {
                    context?.getString(R.string.frag_support_call_now) -> listner.onCallClick()
                    context?.getString(R.string.live_chat) -> listner.onChatClick()
                    context?.getString(R.string.frag_support_email_us) -> listner.onEmailClick()
                    context?.getString(R.string.frag_support_faq) -> listner.onFAQClick()
                }
            }
        }
    }

}

