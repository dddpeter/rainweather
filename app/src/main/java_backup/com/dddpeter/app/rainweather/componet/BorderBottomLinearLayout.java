package com.dddpeter.app.rainweather.componet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.dddpeter.app.rainweather.R;


public class BorderBottomLinearLayout extends LinearLayout {


    public BorderBottomLinearLayout(Context context) {
        super(context);
    }

    public BorderBottomLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BorderBottomLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BorderBottomLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.mybord, null));
        int sroke_width = 1;
        canvas.drawLine(-25, this.getHeight() - sroke_width, this.getWidth() - sroke_width, this.getHeight() - sroke_width, paint);
        super.onDraw(canvas);
    }

}
