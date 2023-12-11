package com.rummytitans.sdk.cardgame.ui.completeprofile

import com.rummytitans.sdk.cardgame.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestionsAdapter(
    private var listResponse: MutableList<String>,
    val listener: OnSuggestionClickListner
) :
    RecyclerView.Adapter<SuggestionsAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestions_rummy, parent, false) as TextView
        return MyViewHolder(textView)
    }

    override fun getItemCount() = listResponse.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = listResponse.get(position)
        holder.textView.setOnClickListener {
            listener.onSuggestionClick(listResponse[position])
        }
    }

    class MyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

}

interface OnSuggestionClickListner {
    fun onSuggestionClick(suggestion: String)
}

