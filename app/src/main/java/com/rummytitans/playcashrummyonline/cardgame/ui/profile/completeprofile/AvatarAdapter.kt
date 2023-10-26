package com.rummytitans.playcashrummyonline.cardgame.ui.completeprofile

import com.rummytitans.playcashrummyonline.cardgame.ui.profile.ProfileSelectListener
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemAvtarRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.AvatarModel

class AvatarAdapter(private var listResponse: MutableList<AvatarModel>?) : RecyclerView.Adapter<com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder>() {
    private var selectedPosition = -1

    private var context: Context? = null
     var listener:ProfileSelectListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder {
        val avatarBinding = ItemAvtarRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return AvatarViewHolder(avatarBinding)
    }

    override fun onBindViewHolder(holder: com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount(): Int {
        return listResponse?.size ?: 0
    }

    fun setSelected(position: Int) {
        selectedPosition = position
        for (avtar in listResponse?.iterator()!!) {
            avtar.isSelected = false
        }
        if (position < 0) return
        try {
            listResponse?.get(position)?.isSelected = true
        } catch (e: Exception) {
        }
        notifyDataSetChanged()
    }

    fun updateItems(listResponse: MutableList<AvatarModel>) {
        this.listResponse?.clear()
        this.listResponse = listResponse
        notifyDataSetChanged()
    }

    inner class AvatarViewHolder internal constructor(internal var mBinding: ItemAvtarRummyBinding) :
        com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder(mBinding.root), AvatarViewModel.AvatarClickListener {
        lateinit var viewModel: AvatarViewModel
        lateinit var model: AvatarModel

        override fun onBind(position: Int) {
            model = listResponse!![position]
            viewModel = AvatarViewModel(model, this)
           // mBinding.viewModel = viewModel
            if (model.isSelected) {
                selectedPosition = position
                viewModel.isSelected.set(true)
            }
            mBinding.executePendingBindings()
        }

        override fun onItemClick() {
            val currentPosition = adapterPosition
            if (currentPosition == selectedPosition) return
            if (selectedPosition != -1) {
                try {
                    listResponse?.get(selectedPosition)?.isSelected = false
                } catch (e: Exception) {
                }
                notifyItemChanged(selectedPosition)
            }
            listResponse!![currentPosition].isSelected = true
            listener?.setAvatarPosition(currentPosition)
            notifyItemChanged(currentPosition)
            viewModel.isSelected.set(true)
            selectedPosition = currentPosition

        }

    }
}

