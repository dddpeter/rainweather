package com.dddpeter.app.rainweather.common;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

public class CommonUtil {
    public static int maxOfAarray(int[] arr) {
        int max = -100;
        for (int i = 0; i < arr.length; i++) {      //遍历
            if (arr[i] >= max) {     //判断当前获取到的值与max值相比较
                max = arr[i];    //max记录最大值
            }
        }
        return max == -100 ? 0 : max;
    }

    public static int minOfAarray(int[] arr) {
        int min = 100;
        for (int i = 0; i < arr.length; i++) {      //遍历
            if (arr[i] <= min) {     //判断当前获取到的值与max值相比较
                min = arr[i];    //max记录最大值
            }
        }
        return min == 100 ? 48 : min;
    }

    public static void fontType(Context ctx, String path, TextView view) {
        Typeface fontFace = Typeface.createFromAsset(ctx.getAssets(), path);
        view.setTypeface(fontFace);
    }

    public static Typeface weatherIconFontFace(Context ctx) {
        Typeface typeface = Typeface.createFromAsset(ctx.getAssets(), "iconfont/iconfont.ttf");
        return typeface;
    }

}
