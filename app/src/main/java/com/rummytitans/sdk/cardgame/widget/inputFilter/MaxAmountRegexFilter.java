package com.rummytitans.sdk.cardgame.widget.inputFilter;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

public class MaxAmountRegexFilter implements InputFilter {

    private int maxAmount = 0;

    private MaxAmountWarningCallback callback;
    public MaxAmountRegexFilter( int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public MaxAmountRegexFilter( int maxAmount, MaxAmountWarningCallback callback) {
        this.maxAmount = maxAmount;
        this.callback = callback;
    }

    @Override
    public CharSequence filter(CharSequence source,
                               int start,
                               int end,
                               Spanned dest,
                               int dstart,
                               int dend) {

        String textToCheck = dest.subSequence(0, dstart).
                toString() + source.subSequence(start, end) +
                dest.subSequence(dstart, dest.length()).toString();

        if(maxAmount > 0 && !TextUtils.isEmpty(textToCheck) && TextUtils.isDigitsOnly(textToCheck)){
            int enteredAmount = Integer.parseInt(textToCheck);
            if(enteredAmount > maxAmount){
                if(callback != null){
                    callback.onMaxAmountEnter();
                }
                return "";
            }
        }
        return null;
    }

    public interface MaxAmountWarningCallback{
        public void onMaxAmountEnter();
    }

}