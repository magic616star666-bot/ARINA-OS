package com.arina.lockscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

public class ArinaLockWallpaperView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ArinaLockWallpaperView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        paint.setShader(new LinearGradient(
                0, 0, w, h,
                new int[] {
                        Color.rgb(4, 9, 18),
                        Color.rgb(8, 20, 38),
                        Color.rgb(10, 22, 42)
                },
                new float[] {0f, 0.58f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);

        paint.setShader(new RadialGradient(
                w * 0.78f, h * 0.16f, w * 0.72f,
                new int[] {
                        Color.argb(110, 53, 198, 255),
                        Color.argb(30, 53, 198, 255),
                        Color.TRANSPARENT
                },
                new float[] {0f, 0.45f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawCircle(w * 0.78f, h * 0.16f, w * 0.72f, paint);

        paint.setShader(new RadialGradient(
                w * 0.15f, h * 0.86f, w * 0.78f,
                new int[] {
                        Color.argb(90, 74, 125, 255),
                        Color.argb(24, 74, 125, 255),
                        Color.TRANSPARENT
                },
                new float[] {0f, 0.52f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawCircle(w * 0.15f, h * 0.86f, w * 0.78f, paint);

        paint.setShader(null);
        paint.setColor(Color.argb(15, 234, 247, 255));
        canvas.drawCircle(w * 0.14f, h * 0.22f, w * 0.12f, paint);
    }
}
