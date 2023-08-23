package com.rummytitans.playcashrummyonline.cardgame.ui.contectList

import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemContactRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.ContactModel
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView

class ContactAdpater(
    var invitesCount: ObservableField<String>?,
    var mContactList: ArrayList<ContactModel>,
    var selectedContacts: ArrayList<ContactModel>,
    val themeColor:ObservableField<String?>
) : RecyclerView.Adapter<com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder>() {

    lateinit var context: Context
    var count = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder {
        context = parent.context
        val binding = ItemContactRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun getItemCount(): Int = mContactList.size

    override fun onBindViewHolder(holder: com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class PlayerViewHolder(private val mview: ItemContactRummyBinding) :
        com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder(mview.root), OnItemClickListener {

        override fun onBind(position: Int) {
            mview.model = mContactList.get(position)
            mview.colorCode=themeColor.get()
            mview.onClick = this
            mview.executePendingBindings()
            if (count != 0)
                invitesCount?.set("+$count")
            else
                invitesCount?.set("")
        }

        override fun onItemSelect() {
            val data = mContactList.get(adapterPosition)
            data.isSelected = data.isSelected == false
            if (data.isSelected) {
                count++
                selectedContacts.add(data)
            } else {
                count--
                selectedContacts.remove(data)
            }
            notifyItemChanged(adapterPosition)
        }
    }

    fun clearSelected() {
        selectedContacts.forEach {
            it.isSelected = false
            notifyItemChanged(mContactList.indexOf(it))
        }
        count = 0
        invitesCount?.set("")
    }
}

interface OnItemClickListener {
    fun onItemSelect()
}

