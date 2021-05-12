package com.dddpeter.app.rainweather.componet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.dddpeter.app.rainweather.R;

public class BorderBottomTextView extends androidx.appcompat.widget.AppCompatTextView {
    private int sroke_width = 1;

    public BorderBottomTextView(Context context) {
        super(context);
    }

    public BorderBottomTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BorderBottomTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.mybord, null));
        canvas.drawLine(-25, this.getHeight() - sroke_width, this.getWidth() - sroke_width, this.getHeight() - sroke_width, paint);
        super.onDraw(canvas);
    }


}
