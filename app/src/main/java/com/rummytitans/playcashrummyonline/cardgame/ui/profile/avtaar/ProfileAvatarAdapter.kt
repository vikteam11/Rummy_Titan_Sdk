package com.rummytitans.playcashrummyonline.cardgame.ui.profile.avtaar

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemAvtarRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder
import com.rummytitans.playcashrummyonline.cardgame.ui.completeprofile.AvatarViewModel

class ProfileAvatarAdapter(var listResponse: MutableList<ProfileAvtarItem>,
                           val callback: AvtarNavigator
) : RecyclerView.Adapter<BaseViewHolder>() {
    private var selectedPosition = -1

    private var context: Context? = null

    init {
       selectedPosition = listResponse.indexOfFirst { it.selected }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val avatarBinding = ItemAvtarRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return AvatarViewHolder(avatarBinding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse.size ?: 0
    }

    fun setSelected(position: Int) {
        listResponse.singleOrNull { it.avtarId == position }?.let {
            it.selected = true
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    fun updateItems(listResponse: MutableList<ProfileAvtarItem>) {
        this.listResponse.clear()
        this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class AvatarViewHolder internal constructor(internal var mBinding: ItemAvtarRummyBinding) :
        BaseViewHolder(mBinding.root), AvatarViewModel.AvatarClickListener {
        lateinit var model: ProfileAvtarItem

        override fun onBind(position: Int) {
            model = listResponse[position]
            mBinding.viewModel = ProfileAvatarViewModel(model,this)
            mBinding.executePendingBindings()
        }

        override fun onItemClick() {
            val currentPosition = layoutPosition
            if (currentPosition == selectedPosition) return
            if (selectedPosition != -1) {
                try {
                    listResponse[selectedPosition].selected = false
                } catch (e: Exception) {
                }
                notifyItemChanged(selectedPosition)
            }
            selectedPosition = currentPosition
            callback.onSelectAvtar(listResponse[currentPosition].avtarId)
            listResponse[currentPosition].selected = true
            notifyItemChanged(currentPosition)
        }
    }
}

interface AvtarNavigator{
    fun onSelectAvtar(id:Int){}
}

