package com.yonisamlan.wearbabytime;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.StringRes;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener {
    private static final String TAG = "babytime";
    private static final long TOAST_RATE_LIMIT_MILLIS = 3500; // AOSP's Toast.LENGTH_LONG
    private static final long MAX_UNLOCK_GESTURE_TIME_MILLIS = TimeUnit.SECONDS.toMillis(5);

    private GestureDetector mGestureDetector;
    private long mLastNotificationTimeMillis;
    private long mUnlockGestureStartedMillis;
    private int mUnlockStepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_baby_time);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 0.0f;
        getWindow().setAttributes(lp);
        mGestureDetector = new GestureDetector(this, this);
        final ViewGroup box = (ViewGroup) findViewById(R.id.box);
        box.setClickable(true);
        box.setFocusable(true);

        box.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Don't allow the system to intercept right-swipes to exit the app.
                box.requestDisallowInterceptTouchEvent(true);
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // Expire the current unlock gesture if it's been a while since we started.
        if (mUnlockGestureStartedMillis > 0 &&
                (SystemClock.elapsedRealtime() - mUnlockGestureStartedMillis
                        > MAX_UNLOCK_GESTURE_TIME_MILLIS)) {
            lock(R.string.toast_timeout);
        }

        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        lock(R.string.toast_babylike_tap);
        return true;
    }

    /**
     * Resets the unlocking process.
     *
     * Also shows a long error toast, but only if we're not in the middle of showing a toast already.
     *
     * @param msg the ID of the string resource to Toast.
     */
    private void lock(@StringRes int msg) {
        mUnlockGestureStartedMillis = 0;
        mUnlockStepCount = 0;

        long nowMillis =  SystemClock.elapsedRealtime();
        if (nowMillis - mLastNotificationTimeMillis > TOAST_RATE_LIMIT_MILLIS) {
            mLastNotificationTimeMillis = nowMillis;
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        lock(R.string.toast_babylike_long_press);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        boolean vertical = Math.abs(velocityX) < Math.abs(velocityY);
        boolean swipeUp = velocityY < 0;

        if (vertical && swipeUp && mUnlockStepCount == 0) { // initial upswipe
            mUnlockGestureStartedMillis = SystemClock.elapsedRealtime();
            mUnlockStepCount++;
        } else if (vertical && swipeUp && mUnlockStepCount == 1) { // second upswipe
            mUnlockStepCount++;
        } else if (vertical && !swipeUp && mUnlockStepCount >= 2) { // downswipe after both ups
            mUnlockStepCount++;
            if (mUnlockStepCount == 4) {
                Toast.makeText(this, R.string.toast_adult_verified, Toast.LENGTH_LONG).show();
                finish();
            }
        } else { // out of order or horizontal swipes
            lock(R.string.toast_babylike_swiping);
        }

        return true;
    }
}
