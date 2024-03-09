package com.rummytitans.sdk.cardgame.ui.rakeback.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.sdk.cardgame.databinding.ItemRakebackBinding
import com.rummytitans.sdk.cardgame.models.RakeBackDetailModelRummy


class RakeBackHistoryAdapter(private var data: MutableList<RakeBackDetailModelRummy.RakeBackHistoryModel>) : RecyclerView.Adapter<RakeBackHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRakebackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun updateData(list: MutableList<RakeBackDetailModelRummy.RakeBackHistoryModel>){
        data= list
        notifyItemRangeChanged(0,data.size)
    }
    inner class ViewHolder(private val binding: ItemRakebackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.model = data[position]
            binding.executePendingBindings()
        }
    }
}