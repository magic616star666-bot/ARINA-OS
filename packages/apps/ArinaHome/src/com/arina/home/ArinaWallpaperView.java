package com.arina.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

public class ArinaWallpaperView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ArinaWallpaperView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        paint.setShader(new LinearGradient(
                0, 0, w, h,
                new int[] {
                        Color.rgb(5, 11, 22),
                        Color.rgb(8, 20, 38),
                        Color.rgb(11, 26, 48)
                },
                new float[] {0f, 0.55f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);

        paint.setShader(new RadialGradient(
                w * 0.80f, h * 0.18f, w * 0.72f,
                new int[] {
                        Color.argb(100, 53, 198, 255),
                        Color.argb(22, 53, 198, 255),
                        Color.TRANSPARENT
                },
                new float[] {0f, 0.45f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawCircle(w * 0.80f, h * 0.18f, w * 0.72f, paint);

        paint.setShader(new RadialGradient(
                w * 0.18f, h * 0.78f, w * 0.66f,
                new int[] {
                        Color.argb(80, 74, 125, 255),
                        Color.argb(16, 74, 125, 255),
                        Color.TRANSPARENT
                },
                new float[] {0f, 0.5f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawCircle(w * 0.18f, h * 0.78f, w * 0.66f, paint);

        paint.setShader(null);
        paint.setColor(Color.argb(18, 234, 247, 255));
        canvas.drawCircle(w * 0.15f, h * 0.16f, w * 0.16f, paint);
    }
}
