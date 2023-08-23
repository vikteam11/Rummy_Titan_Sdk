package com.rummytitans.playcashrummyonline.cardgame.utils

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun startTimerCountdown(
    startTime: String, currentTime: Long? = MyConstants.CURRENT_TIME.value,
    _timerText: MutableLiveData<String>, _timerFinished: MutableLiveData<Boolean>,
    _timerMatchCheck: MutableLiveData<Boolean>? = null, deadLine: Long? = null,
    matchTimeType: Int
): CountDownTimer? {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy H:m:s", Locale.getDefault())
    try {
        val timerCount = dateFormat.parse(startTime).time - currentTime!!
        var hms: String
        var checkM = true
        return object : CountDownTimer(timerCount, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                if (checkM) {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    if (minutes <= deadLine ?: 60L) {
                        checkM = false
                        _timerMatchCheck?.value = true
                    }
                }

                if (hours < 1) {
                    hms = String.format(
                        "%02dm : %02ds",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                        ),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        )
                    )
                } else if (matchTimeType == MyConstants.FULL_TIME_TYPE) {
                    if (hours > 24) {
                        hms = String.format(
                            "%01dd : %02dh : %02dm : %02ds",
                            TimeUnit.MILLISECONDS.toDays(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished) - TimeUnit.MILLISECONDS.toDays(
                                millisUntilFinished
                            ) * 24,
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                            ),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            )
                        )
                    } else {
                        hms = String.format(
                            "%02dh : %02dm : %02ds", hours,
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                            ),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            )
                        )
                    }
                } else hms = setTimeByType(startTime, matchTimeType)

                _timerText.value = hms
            }

            override fun onFinish() {
                _timerFinished.value = true
            }
        }.start()

    } catch (ex: Exception) {
        return null
    }

}


fun startTimerCountdownForQuiz(
    startTime: String, currentTime: Long? = MyConstants.CURRENT_TIME.value,
    _timerText: MutableLiveData<String>, _timerFinished: MutableLiveData<Boolean>,
    _timerMatchCheck: MutableLiveData<Boolean>? = null, deadLine: Long? = null,
    matchTimeType: Int, remainingSeconds: MutableLiveData<Long>
): CountDownTimer? {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy H:m:s", Locale.getDefault())
    try {
        val timerCount = dateFormat.parse(startTime)!!.time - currentTime!!
        var hms: String
        var checkM = true
        return object : CountDownTimer(timerCount, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val second = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                if (second < 11) remainingSeconds.value = second
                if (checkM) {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                    if (minutes <= deadLine ?: 60L) {
                        checkM = false
                        _timerMatchCheck?.value = true
                    }
                }

                if (hours < 1) {
                    hms = String.format(
                        "%02dm : %02ds",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                        ),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        )
                    )
                } else if (matchTimeType == MyConstants.FULL_TIME_TYPE) {
                    if (hours > 24) {
                        hms = String.format(
                            "%01dd : %02dh : %02dm : %02ds",
                            TimeUnit.MILLISECONDS.toDays(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished) - TimeUnit.MILLISECONDS.toDays(
                                millisUntilFinished
                            ) * 24,
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                            ),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            )
                        )
                    } else {
                        hms = String.format(
                            "%02dh : %02dm : %02ds", hours,
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                            ),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                            )
                        )
                    }
                } else hms = setTimeByType(startTime, matchTimeType)
                _timerText.value = hms
            }

            override fun onFinish() {
                remainingSeconds.value = 0
                _timerFinished.value = true
            }
        }.start()

    } catch (ex: Exception) {
        return null
    }
}
