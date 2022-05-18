package com.dddpeter.app.rainweather.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;


public class CommonUtil {
    public static int maxOfAarray(int[] arr) {
        int max = -100;
        for (int j : arr) {      //遍历
            if (j >= max) {     //判断当前获取到的值与max值相比较
                max = j;    //max记录最大值
            }
        }
        return max == -100 ? 0 : max;
    }

    public static int minOfAarray(int[] arr) {
        int min = 100;
        for (int j : arr) {      //遍历
            if (j <= min) {     //判断当前获取到的值与max值相比较
                min = j;    //max记录最大值
            }
        }
        return min == 100 ? 48 : min;
    }

    public static void fontType(Context ctx, String path, TextView view) {
        Typeface fontFace = Typeface.createFromAsset(ctx.getAssets(), path);
        view.setTypeface(fontFace);
    }

    public static Typeface weatherIconFontFace(Context ctx) {
        return Typeface.createFromAsset(ctx.getAssets(), "iconfont/iconfont.ttf");
    }

    public static int[] getScreenSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        // 方法一(推荐使用)使用Point来保存屏幕宽、高两个数据
        Point outSize = new Point();
        // 通过Display对象获取屏幕宽、高数据并保存到Point对象中
        display.getSize(outSize);
        // 从Point对象中获取宽、高
        int x = outSize.x;
        int y = outSize.y;
        return new int[]{x, y};
    }

    public static Drawable drawableFromAssets(Context context, String name) {
        AssetManager manager = context.getAssets();
        InputStream is = null;
        try {
            is = manager.open(name);
            return Drawable.createFromStream(is, name);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
