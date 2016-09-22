package com.android.support;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;

import com.android.emobilepos.R;

import java.util.List;

public class MyKeyboardView extends KeyboardView {

    public MyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            if (key.codes[0] == 7) {
                Log.e("KEY", "Drawing key with code " + key.codes[0]);
                Drawable dr = (Drawable) this.getContext().getResources().getDrawable(R.drawable.white_wall);
                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                dr.draw(canvas);
            } else {
                Drawable dr = (Drawable) getContext().getResources().getDrawable(R.drawable.white_wall);
                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                dr.draw(canvas);
            }
            Paint paint = new Paint();
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(48);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setColor(Color.DKGRAY);
            key.iconPreview = getContext().getResources().getDrawable(R.drawable.white_wall);
            if (key.label != null) {
                canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                        key.y + (key.height / 2), paint);
            }

            //else {
//                key.icon.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
//                key.icon.draw(canvas);
//            }
        }
    }
}
