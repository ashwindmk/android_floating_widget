package com.ashwin.floatingwidget;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

public class FloatingWidgetService extends Service implements View.OnClickListener {
    private WindowManager windowManager;
    private View floatingWidget;
    private View collapsedView;
    private View expandedView;

    @Override
    public void onCreate() {
        super.onCreate();

        floatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget, null);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingWidget, params);

        collapsedView = floatingWidget.findViewById(R.id.collapsed_layout);
        expandedView = floatingWidget.findViewById(R.id.expanded_layout);

        expandedView.setOnClickListener(this);
        floatingWidget.findViewById(R.id.close_button).setOnClickListener(this);

        floatingWidget.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.w(Constants.APP_TAG, "onTouch | event: ACTION_DOWN | param: (" + params.x + ", " + params.y + ")");
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        Log.w(Constants.APP_TAG, "onTouch | event: ACTION_MOVE | before param: (" + params.x + ", " + params.y + ") | raw: (" + event.getRawX() + ", " + event.getRawY() + ")");
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingWidget, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        Log.w(Constants.APP_TAG, "onTouch | event: ACTION_UP: params: (" + params.x + ", " + params.y + ")");
                        collapsedView.setVisibility(View.GONE);
                        expandedView.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collapsed_layout:
                Log.w(Constants.APP_TAG, "onClick | id: collapsed_layout");
                break;

            case R.id.expanded_layout:
                Log.w(Constants.APP_TAG, "onClick | id: expanded_layout");
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                break;

            case R.id.close_button:
                Log.w(Constants.APP_TAG, "onClick | id: close_button");
                stopSelf();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingWidget != null) {
            windowManager.removeView(floatingWidget);
        }
    }
}
