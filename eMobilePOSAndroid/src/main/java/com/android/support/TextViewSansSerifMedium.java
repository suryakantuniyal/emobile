package com.android.support;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Guarionex on 3/7/2016.
 */
public class TextViewSansSerifMedium extends TextView{
    public TextViewSansSerifMedium(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createFont();
    }

    public TextViewSansSerifMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        createFont();
    }

    public TextViewSansSerifMedium(Context context) {
        super(context);
        createFont();
    }

    public void createFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "Comme-Medium.ttf");

        setTypeface(font);
    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        int yOffset = getHeight() - getBaseline();
//        canvas.translate(0, yOffset);
//        super.onDraw(canvas);
//    }
}
