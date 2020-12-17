package com.nmferrer.sundrop.experiments;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import com.nmferrer.sundrop.R;

//TODO: MAKE SELECTION MORE ACCURATE USING DY/DX\

public class RadialMenu extends AppCompatActivity {
    private static final String DEBUG_TAG = "Velocity";
    private VelocityTracker mVelocityTracker = null;

    private ImageView noSelect;
    private ImageView upSelect;
    private ImageView rightSelect;
    private ImageView downSelect;
    private ImageView leftSelect;
    private ImageView upleftSelect;
    private ImageView uprightSelect;
    private ImageView downleftSelect;
    private ImageView downrightSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);

        noSelect    = findViewById(R.id.unselected);
        upSelect    = findViewById(R.id.up_select);
        rightSelect = findViewById(R.id.right_select);
        downSelect  = findViewById(R.id.down_select);
        leftSelect  = findViewById(R.id.left_select);

        upleftSelect = findViewById(R.id.upleft_select);
        uprightSelect = findViewById(R.id.upright_select);
        downleftSelect = findViewById(R.id.downleft_select);
        downrightSelect = findViewById(R.id.downright_select);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        float xVel, yVel;

        switch(action) {
            case MotionEvent.ACTION_DOWN:

                upSelect.setVisibility(View.INVISIBLE);
                downSelect.setVisibility(View.INVISIBLE);
                rightSelect.setVisibility(View.INVISIBLE);
                leftSelect.setVisibility(View.INVISIBLE);

                upleftSelect.setVisibility(View.INVISIBLE);
                uprightSelect.setVisibility(View.INVISIBLE);
                downleftSelect.setVisibility(View.INVISIBLE);
                downrightSelect.setVisibility(View.INVISIBLE);

                if(mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                xVel = mVelocityTracker.getXVelocity(pointerId);
                yVel = mVelocityTracker.getYVelocity(pointerId);
                Log.d(DEBUG_TAG, "X velocity: " + xVel + " | Y velocity: " + yVel);

                boolean xBelowThreshold, yBelowThreshold, xAboveThreshold, yAboveThreshold;
                xBelowThreshold = Math.abs(xVel) < 400;
                yBelowThreshold = Math.abs(yVel) < 400;
                xAboveThreshold = Math.abs(xVel) > 400;
                yAboveThreshold = Math.abs(yVel) > 400;

                if(xBelowThreshold && yAboveThreshold) {
                    if (yVel < 0) {
                        showUp();
                    } else {
                        showDown();
                    }
                }
                if(xAboveThreshold && yBelowThreshold) {
                    if (xVel < 0) {
                        showLeft();
                    } else {
                        showRight();
                    }
                }
                if (xAboveThreshold && yAboveThreshold){
                    if(xVel < 0 && yVel < 0) {
                        showUpLeft();
                    } else if (xVel > 0 && yVel < 0) {
                        showUpRight();
                    } else if (xVel > 0 && yVel > 0) {
                        showDownRight();
                    } else
                        showDownLeft();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
        }
        return true;
    }

    private void showUp() {
        upSelect.setVisibility(View.VISIBLE);
        downSelect.setVisibility(View.INVISIBLE);
        leftSelect.setVisibility(View.INVISIBLE);
        rightSelect.setVisibility(View.INVISIBLE);
        hideIntercardinals();
    }
    private void showDown() {
        upSelect.setVisibility(View.INVISIBLE);
        downSelect.setVisibility(View.VISIBLE);
        leftSelect.setVisibility(View.INVISIBLE);
        rightSelect.setVisibility(View.INVISIBLE);
        hideIntercardinals();
    }
    private void showLeft() {
        upSelect.setVisibility(View.INVISIBLE);
        downSelect.setVisibility(View.INVISIBLE);
        leftSelect.setVisibility(View.VISIBLE);
        rightSelect.setVisibility(View.INVISIBLE);
        hideIntercardinals();
    }
    private void showRight() {
        upSelect.setVisibility(View.INVISIBLE);
        downSelect.setVisibility(View.INVISIBLE);
        leftSelect.setVisibility(View.INVISIBLE);
        rightSelect.setVisibility(View.VISIBLE);
        hideIntercardinals();
    }
    private void showUpLeft() {
        upleftSelect.setVisibility(View.VISIBLE);
        downleftSelect.setVisibility(View.INVISIBLE);
        uprightSelect.setVisibility(View.INVISIBLE);
        downrightSelect.setVisibility(View.INVISIBLE);
        hideCardinals();
    }
    private void showUpRight() {
        upleftSelect.setVisibility(View.INVISIBLE);
        downleftSelect.setVisibility(View.INVISIBLE);
        uprightSelect.setVisibility(View.VISIBLE);
        downrightSelect.setVisibility(View.INVISIBLE);
        hideCardinals();
    }
    private void showDownLeft() {
        upleftSelect.setVisibility(View.INVISIBLE);
        downleftSelect.setVisibility(View.VISIBLE);
        uprightSelect.setVisibility(View.INVISIBLE);
        downrightSelect.setVisibility(View.INVISIBLE);
        hideCardinals();
    }
    private void showDownRight() {
        upleftSelect.setVisibility(View.INVISIBLE);
        downleftSelect.setVisibility(View.INVISIBLE);
        uprightSelect.setVisibility(View.INVISIBLE);
        downrightSelect.setVisibility(View.VISIBLE);
        hideCardinals();
    }

    private void hideCardinals() {
        upSelect.setVisibility(View.INVISIBLE);
        downSelect.setVisibility(View.INVISIBLE);
        leftSelect.setVisibility(View.INVISIBLE);
        rightSelect.setVisibility(View.INVISIBLE);
    }
    private void hideIntercardinals() {
        upleftSelect.setVisibility(View.INVISIBLE);
        downleftSelect.setVisibility(View.INVISIBLE);
        uprightSelect.setVisibility(View.INVISIBLE);
        downrightSelect.setVisibility(View.INVISIBLE);
    }
}