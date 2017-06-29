package org.traccar.manager.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.traccar.manager.R;

/**
 * Created by silence12 on 26/6/17.
 */

public class LUtils {
    private static final int[] STATE_CHECKED = new int[]{android.R.attr.state_checked};
    private static final int[] STATE_UNCHECKED = new int[]{};

    private static Typeface sMediumTypeface;

    protected AppCompatActivity mActivity;
    private Handler mHandler = new Handler();

    private LUtils(AppCompatActivity activity) {
        mActivity = activity;
    }

    public static LUtils getInstance(AppCompatActivity activity) {
        return new LUtils(activity);
    }

    private static boolean hasL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void startActivityWithTransition(Intent intent, final View clickedView,
                                            final String transitionName) {
        ActivityOptions options = null;
        if (hasL() && clickedView != null && !TextUtils.isEmpty(transitionName)) {
//            options = ActivityOptions.makeSceneTransitionAnimation(
//                    mActivity, clickedView, transitionName);
        }

        mActivity.startActivity(intent, (options != null) ? options.toBundle() : null);
    }

    public void setMediumTypeface(TextView textView) {
        if (hasL()) {
            if (sMediumTypeface == null) {
                sMediumTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
            }

            textView.setTypeface(sMediumTypeface);
        } else {
            textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        }
    }

    public int getStatusBarColor() {
        if (!hasL()) {
            // On pre-L devices, you can have any status bar color so long as it's black.
            return Color.BLACK;
        }

        return mActivity.getWindow().getStatusBarColor();
    }

    public void setStatusBarColor(int color) {
        if (!hasL()) {
            return;
        }

        mActivity.getWindow().setStatusBarColor(color);
    }

    public void setOrAnimatePlusCheckIcon(final FloatingActionButton fab, boolean isCheck,
                                          boolean allowAnimate) {
        if (!hasL()) {
            compatSetOrAnimatePlusCheckIcon(fab, isCheck, allowAnimate);
            return;
        }

        Drawable drawable = fab.getDrawable();
        if (!(drawable instanceof AnimatedStateListDrawable)) {
            Resources res = mActivity.getResources();
            drawable = res.getDrawable(R.drawable.ic_truck);
//            drawable.setTint(res.getColor(R.color.fab_icon_color));
            fab.setImageDrawable(drawable);
        }

        if (allowAnimate) {
            drawable.setState(isCheck ? STATE_UNCHECKED : STATE_CHECKED);
            drawable.jumpToCurrentState();
            drawable.setState(isCheck ? STATE_CHECKED : STATE_UNCHECKED);
        } else {
            drawable.setState(isCheck ? STATE_CHECKED : STATE_UNCHECKED);
            drawable.jumpToCurrentState();
        }
    }

    public void compatSetOrAnimatePlusCheckIcon(final ImageView imageView, boolean isCheck,
                                                boolean allowAnimate) {
        final int imageResId = isCheck
                ? R.drawable.back_grey
                : R.drawable.back_white;

        if (imageView.getTag() != null) {
            if (imageView.getTag() instanceof Animator) {
                Animator anim = (Animator) imageView.getTag();
                anim.end();
                imageView.setAlpha(1f);
            }
        }

        if (allowAnimate && isCheck) {
            int duration = mActivity.getResources().getInteger(
                    android.R.integer.config_shortAnimTime);
            Animator outAnimator = ObjectAnimator.ofFloat(imageView, View.ALPHA, 0f);
            outAnimator.setDuration(duration / 2);
            outAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    imageView.setImageResource(imageResId);
                }
            });

            AnimatorSet inAnimator = new AnimatorSet();
            outAnimator.setDuration(duration);
            inAnimator.playTogether(
                    ObjectAnimator.ofFloat(imageView, View.ALPHA, 1f),
                    ObjectAnimator.ofFloat(imageView, View.SCALE_X, 0f, 1f),
                    ObjectAnimator.ofFloat(imageView, View.SCALE_Y, 0f, 1f)
            );

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(outAnimator, inAnimator);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    imageView.setTag(null);
                }
            });
            imageView.setTag(set);
            set.start();
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageResource(imageResId);
                }
            });
        }
    }
}
