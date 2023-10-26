package com.rummytitans.playcashrummyonline.cardgame.ui.settings.adapter

import com.rummytitans.playcashrummyonline.cardgame.R
import com.rummytitans.playcashrummyonline.cardgame.data.SharedPreferenceStorageRummy
import com.rummytitans.playcashrummyonline.cardgame.databinding.ItemNotificaitonSettingRummyBinding
import com.rummytitans.playcashrummyonline.cardgame.models.SubscriptionItemModel
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.rummytitans.playcashrummyonline.cardgame.ui.base.BaseViewHolder

class NotificationAdapter(
    val mList: List<SubscriptionItemModel>,
    val isUserSubscribe: ObservableBoolean,
    val mTopicsSet: MutableLiveData<HashSet<String>>,
    val preferences: SharedPreferenceStorageRummy
) :
    RecyclerView.Adapter<BaseViewHolder>() {
    lateinit var context: Context
    val mTopicsContainer = HashSet<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context
        val binding =
            ItemNotificaitonSettingRummyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return NotificationHolder(binding)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    inner class NotificationHolder(private val mView: ItemNotificaitonSettingRummyBinding) :
        BaseViewHolder(mView.root) {

        override fun onBind(position: Int) {
            val data = mList[position]
            mView.model = data
            changeSwitchTheme()
            mView.executePendingBindings()
            if (data.Status) {
                mView.stateOnOff.isChecked = true
                mTopicsContainer.add(data.mTopicName)
            } else {
                mView.stateOnOff.isChecked = false
                mTopicsContainer.remove(data.mTopicName)
            }
            mView.stateOnOff.setOnCheckedChangeListener { compoundButton, boolean ->
                mList[position].let {
                    if (compoundButton.isPressed) {
                        if (boolean) {
                            // selectItem()
                            mTopicsContainer.add(it.mTopicName)
                        } else {
                            //  removeItem()
                            mTopicsContainer.remove(it.mTopicName)
                        }
                        it.Status = boolean
                        isUserSubscribe.set(boolean)
                        mTopicsSet.value = mTopicsContainer
                    }
                }
            }
        }

        fun changeSwitchTheme() {
            preferences.selectedTheme?.let {
                val (regularTheme, safeTheme) = when (it) {
                    0 -> Pair(R.color.theme1_regular, R.color.theme1_save)
                    1 -> Pair(R.color.theme2_regular, R.color.theme2_save)
                    2 -> Pair(R.color.theme3_regular, R.color.theme3_save)
                    3 -> Pair(R.color.theme4_regular, R.color.theme4_save)
                    4 -> Pair(R.color.theme5_regular, R.color.theme5_save)
                    else -> Pair(R.color.theme1_regular, R.color.theme1_save)
                }

                val thumbStates = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_enabled),
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf()
                    ),
                    intArrayOf(
                        Color.WHITE,
                        ContextCompat.getColor(
                            context,
                            if (!preferences.onSafePlay) safeTheme else regularTheme
                        ),
                        Color.GRAY
                    )
                )
                mView.stateOnOff.thumbTintList = thumbStates
                mView.stateOnOff.thumbTintMode = PorterDuff.Mode.SRC_IN
            }
        }
    }


}
