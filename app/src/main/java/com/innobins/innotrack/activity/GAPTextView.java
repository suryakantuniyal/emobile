package com.innobins.innotrack.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by surya on 19/9/17.
 */

public class GAPTextView extends android.support.v7.widget.AppCompatTextView {

    public GAPTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createFont();

}
    public GAPTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createFont();
    }
    public GAPTextView(Context context) {
        super(context);
        createFont();
    }

    public void setTypeface(int normal) {
    }

    public void createFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "Domine-Bold.ttf");
        setTypeface(font);
    }
}
