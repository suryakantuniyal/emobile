package in.gtech.gogeotrack.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;

/**
 * Created by surya on 19/9/17.
 */

public class MyCustomTextView extends GAPTextView {

    private Runnable characterAdder = new C05301();
    private long mDelay = 500;
    private Handler mHandler = new Handler();
    private int mIndex;
    private CharSequence mText;
    private Context context;

    public MyCustomTextView(Context context) {
        super(context);
        this.context = context;
    }

    class C05301 implements Runnable {
        C05301() {
        }

        public void run() {
            MyCustomTextView.this.setText(MyCustomTextView.this.mText.subSequence(0, MyCustomTextView.this.mIndex = MyCustomTextView.this.mIndex + 1));
            if (MyCustomTextView.this.mIndex <= MyCustomTextView.this.mText.length()-1) {
                MyCustomTextView.this.mHandler.postDelayed(MyCustomTextView.this.characterAdder, MyCustomTextView.this.mDelay);
            }
        }
    }

    public MyCustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.createFromAsset(context.getAssets(), "Domine-Regular.ttf"));
    }

    public void animateText(CharSequence text) {
        this.mText = text;
        this.mIndex = 0;
        setText("");
        this.mHandler.removeCallbacks(this.characterAdder);
        this.mHandler.postDelayed(this.characterAdder, this.mDelay);
    }

    public void setCharacterDelay(long millis) {
        this.mDelay = millis;
    }
}