package com.rummytitans.playcashrummyonline.cardgame.widget.inputFilter;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputRegexFilter implements InputFilter {

    private Pattern mPattern;

    public InputRegexFilter(String pattern) {
        this(Pattern.compile(pattern));
    }

    public InputRegexFilter(Pattern pattern) {
        mPattern = pattern;
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

        Matcher matcher = mPattern.matcher(textToCheck);
        if (!matcher.matches()) {
            return "";
        }
        return null;
    }

}