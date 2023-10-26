package com.rummytitans.playcashrummyonline.cardgame.ui.settings.adapter

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemTimeTypeRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView


data class MatchTimeTimeModel(
    val text: String,
    val isSelected: ObservableBoolean = ObservableBoolean(false),
    val textColor: ObservableField<String>
)
class MatchTimeTypeAdapter(
    val requestFieldText: ObservableField<String>,
    val mPref: SharedPreferenceStorageRummy?,
    val themeColor: ObservableField<String>
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    var prePos: Int = mPref?.matchTimeType ?: 1
    lateinit var context: Context
    val mTimeTypeList = ArrayList<MatchTimeTimeModel>()

    init {
        mTimeTypeList.add(MatchTimeTimeModel("1D : 1H : 30M : 30S", textColor = themeColor))
        mTimeTypeList.add(MatchTimeTimeModel("1 hour left", textColor = themeColor))
        mTimeTypeList.add(MatchTimeTimeModel("12 Sep 3:30pm", textColor = themeColor))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        val binding =
            ItemTimeTypeRummyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class PlayerViewHolder(private val mView: ItemTimeTypeRummyBinding) :
        BaseViewHolder(mView.root) {

        override fun onBind(position: Int) {
            mView.model = mTimeTypeList.get(position)
            if (prePos == position) {
                requestFieldText.set(mTimeTypeList.get(position).text)
                mView.timeType.setTextColor(Color.parseColor(mPref?.regularColor))
                mView.selection.setBackgroundResource(R.drawable.selcted_radio_btn)
                mTimeTypeList.get(adapterPosition).isSelected.set(true)
            } else {
                val res = mView.root.context.resources
                mView.timeType.setTextColor(res.getColor(R.color.cool_grey))
                mView.selection.setBackgroundResource(R.drawable.unselected_radio_btn)
                mTimeTypeList.get(adapterPosition).isSelected.set(false)
            }
            mView.root.setOnClickListener {
                if (prePos == -1) {
                    prePos = adapterPosition
                    notifyItemChanged(prePos)
                } else if (prePos != adapterPosition) {
                    notifyItemChanged(prePos)
                    prePos = adapterPosition
                    notifyItemChanged(adapterPosition)
                }
                mPref?.matchTimeType = adapterPosition
            }
        }
    }

}